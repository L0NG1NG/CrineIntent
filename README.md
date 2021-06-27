# CrineIntent

### 挑战练习：使用Navigation组件与实现高效的RecyclerView刷新

在使用Navigation传递crimeId的时候没有使用推荐的safeArg的方式，简单起见直接使用bundle。

遇到了一个坑就是在FragmentContainerView里的andrlid:name声明成了CrimeListFragment，导致我后面在获取NavController总是获取不到。
完整的应该是android:name="androidx.navigation.fragment.NavHostFragment"

***

ListAdapter能找出支持RecyclerView的新旧数据之间的差异，而且新旧数据的比较在后台完成，不会拖慢UI反应。
只要实现DiffUtil.ItemCallback<T>,而且实现ListAdapter的继承也省事了不少。
不得不说### 强啊


