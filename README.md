[![](https://jitpack.io/v/zj565061763/datastore.svg)](https://jitpack.io/#zj565061763/datastore)

# About

基于`androidx.datastore`封装的对象数据存储库，采用json的格式保存数据。

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
@DatastoreType(id = "")
data class UserInfo(
    val age: Int,
)
```

* 数据对象必须是不可变的，例如使用`data class`并用`val`定义属性
* 使用`DatastoreType`注解标注类，注解中的`id`默认为空，为空的时候库内部会使用被标注类的全类名作为`id`

### 获取Api

```kotlin
// 简化的写法，表示获取默认分组下类型为 UserInfo 的默认文件Api
val userInfoDefaultApi = FDatastore.defaultGroupApi(UserInfo::class.java)
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

# 数据存储位置

`/data/data/包名/files/f_datastore/分组/类型/文件`

* `分组`

  路径中的`分组`字符串为分组名称`MD5`之后的字符串，默认分组为：`com.sd.lib.datastore.group.default`

* `类型`

  路径中的`类型`字符串为`DatastoreType`注解的`id`值`MD5`之后的字符串，默认`id`为空，库内部会使用被标注类的全类名作为`id`

* `文件`

  路径中的`文件`字符串为`TypedDatastore.api(file)`传入的参数`MD5`之后的字符串，如果调用的是无参数的重载函数，则默认值为被标注类的全类名，最终数据存放在该文件中

### 获取Api的完整写法

```kotlin
// 获取默认分组
val group: DatastoreGroup = FDatastore.defaultGroup()

// 获取 UserInfo 类型的存储
val typedDatastore: TypedDatastore<UserInfo> = group.type(UserInfo::class.java)

// UserInfo 类型的默认文件Api
val defaultApi: DatastoreApi<UserInfo> = typedDatastore.api()

// UserInfo 类型的指定文件Api
val fileApi: DatastoreApi<UserInfo> = typedDatastore.api(file = "hello")
```

```kotlin
// 简化的写法，表示获取默认分组下类型为 UserInfo 的默认文件Api，相当于上面代码中的 defaultApi
val userInfoDefaultApi = FDatastore.defaultGroupApi(UserInfo::class.java)
```