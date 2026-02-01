import re
from datetime import datetime


def sql_one(template, obj):
    if not template or not obj:
        return ""

    regex = r"\{(\w+)\}"
    sql = template

    matches = re.findall(regex, template)
    for field_name in matches:
        placeholder = f"{{{field_name}}}"
        if field_name in obj:
            value = obj[field_name]
            formatted_value = sql_format_value(value)
            sql = sql.replace(placeholder, formatted_value)
        else:
            sql = sql.replace(placeholder, "NULL")

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

def sql_format_value(value):
    if value is None:
        return "NULL"
    if isinstance(value, str):
        return "'" + value.replace("'", "''") + "'"
    if isinstance(value, bool):
        return "TRUE" if value else "FALSE"
    if isinstance(value, (int, float)):
        return str(value)
    if isinstance(value, datetime):
        return f"'{value.strftime('%Y-%m-%d %H:%M:%S')}'"
    return f"'{str(value)}'"