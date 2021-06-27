# CrineIntent

### 挑战练习：RecyclerView的ViewType

1.在创建了有联系警方的ViewHolder后，就有了两种ViewHolder，在Recycler.Adaper的<>里声明的ViewHolde不要具体子型改成RecyclerView.ViewHolder。

2.在getItemViewType(Int)可以根据crime是否requiresPolice来返回相对应的值，我直接用了对应的viewholder的layout id。

3.在onCreateViewHolder用这个id来判断创建哪种ViewHolder

4.最后在onBindViewHolder的判断一下holder是两种中的哪种实例后就可以转成具体的ViewHolder进行数据的绑定。


