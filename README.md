# CrineIntent
### 挑战练习:又一个隐式intent(拨打嫌疑人电话)

我感觉这是最难的了，不知道我这样做的方法对不对。对ContactsContract感觉有点懵。

首先是要在fragment中去申请读取联系人权限，才发现原来registerForActivityResult不仅可以取代startActivityForResult，还能用来很方便的进行权限申请。

要得到手机号按照书上思路是要去查两次表，第一次通过联系人名字去找id,
后面用id去CommonDataKinds.Phone表里找电话号。多亏Kotllin里cursor.use这个东西，最后代码看起来还算优雅。
