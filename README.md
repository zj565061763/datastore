[![](https://jitpack.io/v/zj565061763/datastore.svg)](https://jitpack.io/#zj565061763/datastore)

# About

基于[datastore](https://developer.android.com/topic/libraries/architecture/datastore)封装的对象存储库，使用json格式保存数据。

# 使用

### 初始化

```kotlin
FDatastore.init(
    context = this,
    onError = {
        // 错误回调
    }
)
```

### 数据对象

```kotlin
@DatastoreType(id = "UserInfo")
data class UserInfo(
    val age: Int,
)
```

* 数据对象必须是不可变的，例如使用`data class`并用`val`定义属性
* 使用`DatastoreType`注解标注类，注解中的`id`必须全局唯一

### 获取Api

```kotlin
val userInfoDatastoreApi: DatastoreApi<UserInfo> = FDatastore.api(UserInfo::class.java)
```

### 使用Api

挂起Api：

```kotlin
lifecycleScope.launch {
    // 获取数据
    val data = api.get()

    api.replace {
        // 替换数据，null-表示清空数据
        null
    }

    api.replace { data ->
        // 替换数据，如果没有数据则data为null
        data?.copy(age = 100)
    }

    api.update { data ->
        // 更新数据，此时本地一定有数据，data不为null，同时返回值也不允许为null
        data.copy(age = 200)
    }
}
```

在协程外调用，可以使用同步Api：

```kotlin
api.getBlocking()
api.replaceBlocking { null }
api.updateBlocking { it.copy(age = 200) }
```