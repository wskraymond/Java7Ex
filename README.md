# Java7Ex


1. What is the difference between a wildcard bound and a type parameter bound?

A wildcard can have only one bound, while a type parameter can have several bounds.

A wildcard can have a lower or an upper bound, while there is no such thing as a lower bound for a type parameter.

http://www.angelikalanger.com/GenericsFAQ/FAQSections/TypeArguments.html#FAQ203

2. When to use generic methods and when to use wild-card?

If you want to enforce some relationship on the different types of method arguments, you can't do that with wildcards, you have to use type parameters.
```public static <T extends Number> void copy(List<T> dest, List<T> src)```
Here, you are ensured that both dest and src have the same parameterized type for List. So, it's safe to copy elements from src to dest.

But, if you go on to change the method to use wildcards:

```public static void copy(List<? extends Number> dest, List<? extends Number> src)```
it won't work as expected. In 2nd case, you can pass List<Integer> and List<Float> as dest and src. So, moving elements from src to dest wouldn't be type-safe anymore. If you don't need such kind of relation, then you are free not to use type parameters at all.

https://stackoverflow.com/questions/18176594/when-to-use-generic-methods-and-when-to-use-wild-card
