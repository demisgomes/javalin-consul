import com.google.common.net.HostAndPort
import com.orbitz.consul.Consul
import com.orbitz.consul.cache.KVCache
import io.javalin.Javalin

var consulValue = "default"

fun main() {
    setupConsul()
    val app = Javalin.create().start(7000)
    app.get("/") { ctx -> ctx.result("Hello World $consulValue") }
}

fun setupConsul(){
    val client = Consul.builder().withHostAndPort(HostAndPort.fromParts("localhost", 8500)).build()
    val kvClient = client.keyValueClient()

    val cache = KVCache.newCache(kvClient, "config/application/",60)
    cache.addListener { newValues ->
        val newValue = newValues.values.stream()
            .filter { value -> value.key == "config/application/key" }
            .findAny()

        newValue.ifPresent { value ->
            val decodedValue = newValue.get().valueAsString
            decodedValue.ifPresent { v -> println(String.format("Value is: %s", v)) }
            consulValue = decodedValue.get()
        }
    }
    cache.start()
}