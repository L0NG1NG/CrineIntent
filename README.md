# CrineIntent
### 挑战练习：时间选择对话框

Easy!没啥好E说的，就跟DatePickerDialog一样。

不过这里在fragment间传递数据没有用setTargetFragment，而是在CrimeFragment(父framgnet)用了parentFragmentManager.setFragmentResultListener，
然后在DatePickFragment(子framgnet)里parentFragmentManager.setFragmentResultListener只要确保两个的REQUEST_CODE一致，
剩下的就交给FramgnetManager来就行了
