1. 有两个 module(A、B，其中 A 依赖 B)都实现了上述动态配置，两个模块 runAlone 配置都为 true，这时我们要运行 A，在编译时会报错。为什么哪？
因为两个 module 此时都为 application，显然是不能互相依赖的。这就需要我们在运行 A 时，让 B 动态的变为 library，这块的处理后期再讲