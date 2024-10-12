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
   val age: Int = 0,
   val name: String = "name",
)
```

* 数据对象必须是不可变的，例如使用`data class`并用`val`定义属性
* 使用`DatastoreType`注解标注类，注解中的`id`必须全局唯一

### 获取Api

```kotlin
val datastoreApi: DatastoreApi<UserInfo> = FDatastore.api(UserInfo::class.java)
```

### 使用Api

挂起Api：

```kotlin
interface DatastoreApi<T> {

   /** 数据流 */
   val dataFlow: Flow<T?>

   /**
    * 数据流，如果数据为空，则调用[factory]创建数据，并根据[save]决定是否保存创建的数据
    */
   fun dataFlow(
      save: Boolean = false,
      factory: () -> T,
   ): Flow<T>

   /** 获取数据 */
   suspend fun get(): T?

   /** 用[data]替换数据 */
   suspend fun replace(data: T?): T?

   /** 用[transform]替换数据 */
   suspend fun replace(transform: suspend (T?) -> T?): T?

   /** 已保存数据不为null，才会调用[transform]更新数据 */
   suspend fun update(transform: suspend (T) -> T): T?
}
```