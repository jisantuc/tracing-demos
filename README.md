# tracingdemos
An API project that explores tracing options with http4s. It doesn't know anything about math or order of operations, it just applies the ops in the order that they're selected.

Example:

```bash
$ http :8080/api/interpreter seed==1234 ops==12
```

produces

```json
{
  "result": 809.7262416652559,
  "operations": {
    "subtract1": {
      "divide1e6By": {
        "divideBy4": {
          "multiplyBy5": {
            "add3": {
              "subtract1": {
                "divide1e6By": {
                  "divideBy4": {
                    "multiplyBy5": {
                      "add3": {
                        "subtract1": {
                          "divide1e6By": {
                            "lit": 1234
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
```
