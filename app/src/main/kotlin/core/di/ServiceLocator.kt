package core.di

object ServiceLocator {
    private val providers: MutableMap<Class<*>, () -> Any> = mutableMapOf()

    @Synchronized
    fun <T : Any> register(clazz: Class<T>, provider: () -> T) {
        providers[clazz] = provider
    }

    @Synchronized
    fun <T : Any> registerSingleton(clazz: Class<T>, instance: T) {
        providers[clazz] = { instance }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> resolve(clazz: Class<T>): T {
        return providers[clazz]?.invoke() as? T
            ?: error("Service not registered: ${clazz.name}")
    }

    @Synchronized
    fun clear() {
        providers.clear()
    }

    inline fun <reified T : Any> register(noinline provider: () -> T) {
        register(T::class.java, provider)
    }

    inline fun <reified T : Any> registerSingleton(instance: T) {
        registerSingleton(T::class.java, instance)
    }

    inline fun <reified T : Any> resolve(): T {
        return resolve(T::class.java)
    }
}
