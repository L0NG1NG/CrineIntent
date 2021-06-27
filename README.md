# CrineIntent

### 挑战练习：优化照片显示

做法就跟之前的DatePickFrament一样还更简单一点

### 挑战练习：优化缩略图加载

使用ViewTreeObserver的OnGlobalLayoutLister(可以监听任何布局传递，控制事件的发生)来得到ImageView的尺寸。

之前考虑的一个问题是匿名函数可以用this返回自己，那lambda表达式怎么的到自身？似乎不行
