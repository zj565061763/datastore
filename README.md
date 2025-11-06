[![](https://jitpack.io/v/zj565061763/datastore.svg)](https://jitpack.io/#zj565061763/datastore)

# About

基于[datastore](https://developer.android.com/topic/libraries/architecture/datastore)封装的对象存储库，使用json格式保存数据。

# 使用

### 初始化

```kotlin
class App : Application() {
   override fun onCreate() {
      super.onCreate()
      // 默认在主进程自动初始化，其他进程需要手动初始化，初始化方法可以重复调用。
      FDatastore.init(this)

      // 监听错误信息
      GlobalScope.launch {
         FDatastore.errorFlow.collect {
            logMsg { "error:${it.stackTraceToString()}" }
         }
      }
   }
}
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
* 建议给所有属性设置默认值，App版本升级时，新增的字段在旧版本上不存在，会使用默认值

### 使用Api

```kotlin
// 获取api
val api: DatastoreApi<UserInfo> = FDatastore.get(UserInfo::class.java)
```

```kotlin
interface DatastoreApi<T> {
   /** 数据流 */
   val flow: Flow<T?>

   /** 用[transform]的结果替换数据 */
   suspend fun replace(transform: suspend (T?) -> T?): T?
}

/** 获取数据 */
suspend fun <T> DatastoreApi<T>.get(): T?

/** 数据不为null，才会调用[transform]更新数据 */
suspend fun <T> DatastoreApi<T>.update(transform: suspend (T) -> T): T? 
```

### 有默认值的Api

```kotlin
// 把一个api转为具有默认值的api
val api: DatastoreApiWithDefault<UserInfo> = FDatastore.get(UserInfo::class.java).withDefault { UserInfo() }
```

```kotlin
interface DatastoreApiWithDefault<T> {
   /** 数据流 */
   val flow: Flow<T>

   /** 用[transform]的结果替换数据 */
   suspend fun update(transform: suspend (T) -> T): T
}

/** 获取数据 */
suspend fun <T> DatastoreApiWithDefault<T>.get(): T
```