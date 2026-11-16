import gc
import importlib.util
import sys

from org.nature.util import PythonUtil
from sql_builder import sql_one, sql_each

sql_ddl_create_table = """
create table if not exists module_script(
    module text not null,
    script text not null,
    status text not null,
    seq text not null,
    primary key (module)
)
"""

PythonUtil.ddl("nature/common.db", sql_ddl_create_table)


def do_load(datum=None, status=None):
    if datum:
        rows = [datum]
    else:
        rows = do_list(status=status or "1")
    for row in rows:
        module = row["module"]
        script = row["script"]
        if not module or not script:
            raise Warning("模块或脚本不可为空")
        spec = importlib.util.spec_from_loader(module, loader=None)
        module_obj = importlib.util.module_from_spec(spec)
        sys.modules[module] = module_obj
        exec(script, module_obj.__dict__)
    return len(rows)


def do_list(status=None, keyword=None):
    sql = "select module,script,status,seq from module_script where 1=1"
    param = {}
    if status:
        sql += " and status={status}"
        param.update({"status": status})
    if keyword:
        sql += " and module like {keyword}"
        param.update({"keyword": "%" + keyword + "%"})
    sql += " order by seq"
    return PythonUtil.list("nature/common.db", sql_one(sql, param))


def do_unload(module):
    del sys.modules[module]
    if module in sys.modules:
        del sys.modules[module]
    globals().pop(module, None)
    gc.collect()


def do_find(module):
    sql = "select module,script,status,seq from module_script where module={module}"
    return PythonUtil.list("nature/common.db", sql_one(sql, {"module": module}))


def do_save(datum):
    _validate(datum)
    return _save([datum])


def do_delete(datum):
    if "module" not in datum or not datum["module"]:
        raise Warning("模块不可为空")
    return _delete(datum)


def do_update(datum):
    _validate(datum)
    exists = do_find(datum["module"])
    if not exists:
        raise Warning(f"模块{datum['module']}不存在")
    return _update(datum)


def _save(data):
    sql = "insert into module_script(module,script,status,seq) values[({module},{script},{status},{seq})]"
    return PythonUtil.update("nature/common.db", sql_each(sql, data))


def _update(datum):
    sql = "update module_script set script={script},status={status},seq={seq} where module={module}"
    return PythonUtil.update("nature/common.db", sql_one(sql, datum))


def _delete(datum):
    sql = "delete from module_script where module={module}"
    return PythonUtil.update("nature/common.db", sql_one(sql, datum))


def _validate(datum):
    if "module" not in datum or not datum["module"]:
        raise Warning("模块不可为空")
    if "script" not in datum or not datum["script"]:
        raise Warning("脚本内容不可为空")
    if "status" not in datum or not datum["status"]:
        raise Warning("状态不可为空")
    if datum["status"] not in ["0", "1"]:
        raise Warning("状态只能为启用或暂停")
    if "seq" not in datum or not datum["seq"]:
        raise Warning("序号不可为空")
