package me.zombii.luak

import jnr.ffi.LibraryLoader
import jnr.ffi.mapper.TypeMapper
import me.zombii.luak.ffi.NativeAPI
import me.zombii.luak.util.LuaState
import me.zombii.luak.util.LuaStateConverter
import java.io.File
import java.nio.file.Files
import java.util.*

object LuaK {

    val nativeAPI: NativeAPI = createLibLoader().load()

    @JvmStatic
    fun main(args: Array<String>) {
        val state = nativeAPI.luaL_newstate()

//        luaC.luaL_requiref(state.luaState, "_G", luaC::luaopen_base, 1)
//        luaC.lua_pop(state.luaState, 1)

        nativeAPI.luaL_openlibs(state)

        val scanner = Scanner(System.`in`)

        println("Started LuaK")

        print("Lua > ")
        while (scanner.hasNextLine()) {
            val line = scanner.nextLine()
            nativeAPI.luaL_dostring(state, line)
            print("Lua > ")
        }
        println("Closing LuaK")

        nativeAPI.lua_close(state)
        scanner.close()
    }

    private fun createLibLoader(): LibraryLoader<NativeAPI> {
        val dir = Files.createTempDirectory(".luaK").toFile()
        println(dir)
        dir.mkdirs()

        val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
        if(osName.contains("windows")) {
            cpyBin(dir, "lua.dll")
        } else if(osName.contains("linux")) {
            cpyBin(dir, "liblua.so")
        }


        return LibraryLoader.create(NativeAPI::class.java).mapper(LuaStateConverter.INSTANCE)
        .mapper(TypeMapper.Builder().map(LuaState::class.java, LuaStateConverter.INSTANCE).build())
        .library("lua")
        .search(dir.absolutePath);
    }

    private fun cpyBin(dir: File, string: String) {
        val file = File(dir, string)

        val bytes = LuaK::class.java.classLoader.getResource(string)!!.readBytes()
        file.createNewFile()
        file.writeBytes(bytes)
    }

}