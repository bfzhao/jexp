# Jexp

Jexp is a Java library expression engine, which implements basic expression engine calculation support and offers optimized handling for JSON data. 

## Basic Types

Jexp supports the following basic types: numeric values, string values, boolean values, null values, sequence values, map values, expression values, and timestamp values. The basic syntax for constructing them is as follows:

```javascript
# Numeric values, supporting integers, floating-point numbers, or floating-point numbers represented in scientific notation
12
-1.3
4e-2

# String values, enclosed in double quotes
"Hello world"

# Boolean values
true
false

# Null values
null

# Sequence values, a sequence of values enclosed in [], which can be of different types or nested
["hello", 23, false, ["world"]]

# Map values, key-value pairs enclosed in {}, where keys must be string values and values can be any valid value
{"name": "Tom", "age": 13, "Address": {"AddressLine": "Ave 24", "Postcode": "01240"}}

# Expression values, a single expression enclosed in @{}
@{ 2 + 2 }

# Timestamp values, there is no direct syntax for constructing timestamp values; they need to be obtained using functions. For example, now() returns the value of the local time
now()
```

It is evident that, apart from expression values, the definitions of all other values are compatible with JSON definitions, ensuring interoperability between Jexp and JSON.

## Expression Construction

Jexp expressions consist of several components: assignment, value operations, JSON filtering, template evaluation, and function calls.

## Assignment

Jexp supports variable definition without the need for explicit declaration before use. Variables can be assigned as follows:

```javascript
x = 12
x*2

# output 24
```

Variables themselves do not have a type; their type depends on the type of the latest bound value. Using an uninitialized variable will trigger an evaluation exception.

## Evaluation Broadcasting

For functions or operators that expect a non-list type, you can pass in an array. The function or operator will evaluate this array element by element and return the final result array. This behavior is known as broadcasting.

Broadcasting applies to most operators and functions. It does not apply when an operator or function expects a list as an argument.

For operators, there are left broadcasting and right broadcasting, which are both supported by default. If both left and right operands are lists, element-wise operations will be performed. In this case, lists of different lengths will trigger an exception.

```javascript
# Left broadcasting, output [2, 4, 6, 8]
[1,2,3,4] * 2

# Right broadcasting, output [3, 2, 1, 0]
4 - [1,2,3,4]

# Element-wise operation, output [3, 1, -1, -3]
[4,3,2,1] - [1,2,3,4]
```

## Value Operations

Jexp supports built-in arithmetic operators for basic operations on value expressions, defined as follows:

|Category | Operator| Description | Broadcasting|
|-|-|-|-|
| Assignment Operator| =| Assigns a constant or variable to a variable name without declaration |No|
| Positive/Negative Sign Operators |+, - ||Yes |
| Arithmetic Operators |+, -, *, /, %, ^| Addition, subtraction, multiplication, division, modulo, exponentiation. Applicable to numeric values | Yes |
| Logical Operators | &&, \|\|, ! | logical and, or, not | yes |
| Comparison Operators |>, >=, ==, <=, <, !=| Greater than, greater than or equal to, equal to, less than or equal to, less than, not equal to operations. Except for equal and not equal, applicable to all types; others are applicable to numeric values | Yes |
| Template Operator| ``| Replaces content using templates, supports JSON operators |No |
| Array Operator |++| Concatenates two arrays into one array |No |
| JSON Operator | $.jpath| Uses JSON PATH syntax | No |
| Sorting Operator | <=> | Compares two values a and b, returning -1, 0, or 1 when a < b, a == b, or a > b, respectively. Used for custom sorting rules in sort | No |
| Date Operator | +, - | + and - can be used to add or subtract an offset from a given date-time value (supports yMdhms suffixes for different offset units); subtracting two date-time values gives their difference | Yes |
| String Operator | + | String concatenation | Yes |
| Chained Call Operator | . | Calls functions using chaining syntax, passing the result before the dot as the first parameter to the function after the dot. Only supports arrays. For example, [].b(x).c(y) is equivalent to c(b([], x), y) | No |

The template operator and JSON operator will be explained separately later on.

## JSON Filtering

Use JSON PATH syntax (`$.(.(name)|[index|filter])*`) to access JSON data in the evaluation context. Examples include:

* `$`: Returns the root JSON data
* `$.XX`: Returns the value of the property named XX under the JSON root
* `$.[]`: Returns elements of an array under the JSON root if it's an array
* `$.[i]`: Returns the i-th element of an array
* `$.[exp]`: Returns array elements based on a filter condition where exp is a valid expression
* `$.XX[i].YY.ZZ[exp]`: Gets the filtered element list of path `XX[i].YY.ZZ`
Optionally, the part after $ can be enclosed in braces, like `${.x}`

Operating on JSON data is a core capability of Jexp. In Jexp, we use jpath to represent filtering conditions for JSON.

```javascript
{
    "x": [
        {
            "y": [1, 2, 3],
            "z": "hello"
        },
        {
            "y": [2],
            "z": false
        },
        {
            "y": null,
            "z": "yeah"
        },
        {
            "y": [1, 2, 1, 2],
            "z": null
        }
    ],
    "s" : {
        "y": 12.0,
        "u": {
            "z": [11.0, 22.4, 33.0],
            "d": 3.14
        }
    },
    "k": 119.0,
    "%%%": 42,
    "#$@#%!!$()": 22,
    "empty": []
}
```

When evaluating in Jexp, it can support a context variable. When a variable name is not specified, by default, it evaluates the JSON in this context. The following examples show the results after different jpath evaluations:
|JPATH|Value|
|-|-|
|`$."%%%"`|42|
|`$.k`|119|
|`$.s.y`|12.0|
|`$.s.u.d`|3.14|
|`$.s.u.z[0]`|11|
|`$.s.u.z[7]`|null|
|`$.s.u.z[7].x`|null|
|`$.s.hello`|null|
|`$.s.u.z[]`|[11.0, 22.4, 33.0]|
|`$.x[1].y[0]`|2|
|`$.x[0].z`|"hello"|
|`$.x[@{$.z == "hello" \|\| $.z == "yeah"}]` | [{"y": [1, 2, 3],"z": "hello"}, {"y": null,"z": "yeah"}] |

Similarly, `$.` returns the complete object. When a variable is of a mapping type, it also supports filter expressions based on JPATH.

```javascript
x = [{"x": 12}, {"y": 42}]
$x[1].y
# Evaluation result: 42
```

In the example expression, there is a special construction `@{}`, which represents an expression value used for setting filtering conditions. Apart from being used for JSON filtering, an expression value can also be used like a single-parameter lambda expression. For example:

```javascript
x = @{ _ + 12 }
x(30)
# Evaluation result: 42
```

The special variable `_` is only valid within expression variables and represents the passed-in parameter. Since JPATH syntax can also represent this, the expression variable can be defined equivalently as `@{ $_ + 12 }`.

## Templates
Templates are a special operator that allows value replacement based on predefined templates to obtain the final result. Templates use the ```` syntax.

```javascript
x = 12
z = {"x": 23, "y": [1,2,3]}
`x=$x, z=$z.y[1]`
# Evaluation result: x=12, z=2
```

As demonstrated, using template evaluation syntax makes it easy to construct JSON strings or format the final output result.

## Function Calls

Jexp provides several built-in basic functions for use. Here is the definition:
|Category|Function Name|Description|Broadcastable|
|-|-|-|-|
|Logarithmic Functions|`log(a)`|Calculate natural logarithm|Yes|
|Logarithmic Functions|`log2(a)`|Calculate logarithm base 2|Yes|
|Logarithmic Functions|`log10(a)`|Calculate logarithm base 10|Yes|
|Logarithmic Functions|`log1p(a)`|Calculate natural logarithm plus one|Yes|
|Logarithmic Functions|`logb(a, b)`|Calculate logarithm with a specified base, log(a)/log(b)|Yes|
|Trigonometric Functions|`sin(a)`|Calculate sine value|Yes|
|Trigonometric Functions|`cos(a)`|Calculate cosine value|Yes|
|Trigonometric Functions|`tan(a)`|Calculate tangent value|Yes|
|Trigonometric Functions|`cot(a)`|Calculate cotangent value|Yes|
|Trigonometric Functions|`sec(a)`|Calculate secant value (reciprocal of sine)|Yes|
|Trigonometric Functions|`csc(a)`|Calculate cosecant value (reciprocal of cosine)|Yes|
|Trigonometric Functions|`asin(a)`|Calculate arcsine value|Yes|
|Trigonometric Functions|`acos(a)`|Calculate arccosine value|Yes|
|Trigonometric Functions|`atan(a)`|Calculate arctangent value|Yes|
|Trigonometric Functions|`sinh(a)`|Calculate hyperbolic sine value|Yes|
|Trigonometric Functions|`cosh(a)`|Calculate hyperbolic cosine value|Yes|
|Trigonometric Functions|`tanh(a)`|Calculate hyperbolic tangent value|Yes|
|Trigonometric Functions|`coth(a)`|Calculate hyperbolic cotangent value|Yes|
|Trigonometric Functions|`sech(a)`|Calculate hyperbolic secant value|Yes|
|Trigonometric Functions|`csch(a)`|Calculate hyperbolic cosecant value|Yes|
|Numeric Functions|`abs(a)`|Calculate absolute value|Yes|
|Numeric Functions|`cbrt(a)`|Calculate cube root value|Yes|
|Numeric Functions|`floor(a)`|Calculate the greatest integer less than or equal to the parameter|Yes|
|Numeric Functions|`ceil(a)`|Calculate the smallest integer greater than or equal to the parameter|Yes|
|Numeric Functions|`pow(a, b)`|Calculate the value of a raised to the power of b|Yes|
|Numeric Functions|`exp(a)`|Calculate the value of e (Euler's number) raised to the power of a|Yes|
|Numeric Functions|`expm1(a)`|Calculate the value of e raised to the power of a minus 1|Yes|
|Numeric Functions|`sqrt(a)`|Calculate square root value|Yes|
|Numeric Functions|`toRedian(a)`|Convert angle to radian|Yes|
|Numeric Functions|`toDegree(a)`|Convert radian to degree|Yes|
|Numeric Functions|`round(a, b, c)`|Round number a to b decimal places, and round according to rounding mode c. Second and third parameters are optional, default is to keep 2 decimal places and use round half up. Supported rounding modes: UP (round up), DOWN (round down), HALF_UP (round half up), HALF_DOWN (round half down)|No|
|Statistical Functions|`max([...])`|Calculate the maximum value of a sequence|No|
|Statistical Functions|`min([...])`|Calculate the minimum value of a sequence|No|
|Statistical Functions|`avg([...])`|Calculate the average value of a sequence|No|
|Statistical Functions|`sum([...])`|Calculate the sum of a sequence|No|
|Type Conversion Functions|`toString(a)`|Convert value to string type|Yes|
|Type Conversion Functions|`toBoolean(a)`|Convert value to boolean type|Yes|
|Type Conversion Functions|`toNumber(a)`|Convert value to numeric type|Yes|
|Statistics Function|`choice(c, a, b)`|Returns either a or b based on the result of expression c|No|
|Statistics Function|`filter([...], f)`|Filters a sequence based on f|No|
|JSON Function|`jsonGet(json, path)`|Extracts a value from JSON using a JSON path|No|
|Set Function|`union([], [])`|Computes the union of two arrays|No|
|Set Function|`intersect([], [])`|Computes the intersection of two arrays|No|
|Set Function|`diff([], [])`|Computes the difference of two arrays|No|
|Set Function|`symDiff([], [])`|Computes the symmetric difference between two arrays, i.e., elements not in the intersection|No|
|Array Function|`count([...])`|Counts the number of elements in a sequence|No|
|Array Function|`contains([...], [...])`/ `contains([...], a)`|Checks if the second sequence or value exists in the first sequence|No|
|Array Function|`sort([], @{})`|Sorts a sequence with an optional argument to control sorting behavior|No|
|Array Function|`uniq([], @{})`|Removes adjacent duplicate elements from a sequence with an optional argument to control deduplication behavior|No|
|Array Function|`concat(a, b, c)`|Combines multiple values into an array where these values can be variables or expressions|No|
|Date/Time Function|`now()`|Gets the current date and time|No|
|Date/Time Function|`toDate(a)`|Converts a string to a date/time object. Supports specific formats like "yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss", "yyyy-MM-dd", "yyyy/MM/dd", "yyyy.MM.dd"|Yes|
|Date/Time Function|`toDateFmt(a, "fmt")`|Converts a string to a date/time object using a custom format|Yes|
|Date/Time Function|`formatDate(a, "fmt")`|Converts a date/time object to a string using a custom format|Yes|
|Array Function|`join([], [], k1, k2)`|Joins two arrays based on keys k1 and k2 from the first and second sequences, respectively|No|
|Array Function|`map([], @{})`|Maps and transforms an array to generate a new array|No|
|Matching Function|`regMatch(a, regex)`|Matches using a regular expression|Yes|
|Array Function|`take([], n)`|Extracts the first n elements from an array|No|

## Acknowledges

This project was triggered by [exp4j](https://github.com/fasseg/exp4j). After have a try, it's very clear that a new project is the answer to my requirement. Many basic unit test cases are borrowed, thanks!

The json path syntax refers [jq](https://github.com/jqlang/jq), one of the best even JSON tools, but not all features included, thanks!

The idea of broadcasting of vector is from [numpy](https://github.com/numpy/numpy), thanks!

The customized sort and compare operator `<=>` is clear borrowed from [Perl](https://github.com/Perl/perl5), thanks!
