import inspect
import json
import sys


def dynamic_exec(code, params):
    scope = {}
    exec(code, scope)
    functions = [i for i in scope.values() if callable(i)]

    if functions:
        func = functions[0]
        sig = inspect.signature(func)
        args = {k: params.get(k) for k in sig.parameters.keys()}
        return func(**args)
    else:
        raise ValueError("提供的代码字符串中没有可执行的函数")


def module_func_exec(module, func, params):
    if module not in sys.modules or not sys.modules[module]:
        raise ValueError(f"模块{module}不存在")
    module_obj = sys.modules[module]
    if not hasattr(module_obj, func):
        raise ValueError(f"模块{module}中不存在函数{func}")
    func_obj = getattr(module_obj, func)
    if not callable(func_obj):
        raise ValueError(f"模块{module}中函数{func}不是可调用对象")
    sig = inspect.signature(func_obj)
    args = {k: params.get(k) for k in sig.parameters.keys()}
    return func_obj(**args)


def to_json(obj):
    return json.dumps(obj, ensure_ascii=False, default=lambda o: float(o))


def get_job_func(code):
    scope = {}
    exec(code, scope)
    job_func = scope.get("do_exec")
    if job_func:
        return job_func
    raise ValueError("函数不存在")
