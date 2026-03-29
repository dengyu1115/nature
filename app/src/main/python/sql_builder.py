import re
from datetime import datetime
from decimal import Decimal


def sql_one(template, obj):
    if not template or not obj:
        return ""

    regex = r"\{(\w+)\}"
    sql = template

    matches = re.findall(regex, template)
    values = {i: do_format(obj[i]) if i in obj else "NULL" for i in matches}
    sql = sql.format(**values)
    return sql


def sql_each(template, items):
    if not template or not items or not isinstance(items, list) or len(items) == 0:
        return ""

    loop_pattern = r"\[([^\[\]]*?\{.*?\}.*?)\]"
    match = re.search(loop_pattern, template)

    if match:
        loop_part = match.group(1)
        before_loop = template[:match.start()]
        after_loop = template[match.end():]
        sql_parts = []
        for item in items:
            part = sql_one(loop_part, item)
            sql_parts.append(part)
        return before_loop + ",".join(sql_parts) + after_loop
    else:
        raise ValueError("模板格式错误")


def do_format(value):
    if value is None:
        return "NULL"
    if isinstance(value, str):
        return "'" + value.replace("'", "''") + "'"
    if isinstance(value, bool):
        return "TRUE" if value else "FALSE"
    if isinstance(value, (int, float, Decimal)):
        return str(value)
    if isinstance(value, datetime):
        return f"'{value.strftime('%Y-%m-%d %H:%M:%S')}'"
    return f"'{str(value)}'"
