import inspect


def dynamic_exec(code_str, params_dict):
    scope = {}
    exec(code_str, scope)

    functions = {k: v for k, v in scope.items() if callable(v)}

    if functions:
        func_name = list(functions.keys())[0]
        func = functions[func_name]

        try:
            sig = inspect.signature(func)
            func_params = list(sig.parameters.keys())

            args_for_func = {}
            for param in func_params:
                args_for_func[param] = params_dict.get(param)

            return func(**args_for_func)
        except ValueError:
            return func()
    else:
        raise ValueError("提供的代码字符串中没有可执行的函数")