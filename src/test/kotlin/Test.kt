
import jnr.ffi.Struct
import jnr.ffi.provider.LoadedLibrary
import me.zombii.luak.LuaK
import me.zombii.luak.java.HelloWorld
import me.zombii.luak.lua.conv.ClassConverter
import me.zombii.luak.lua.conv.StaticObject
import me.zombii.luak.types.functions.Lua_CFunction
import me.zombii.luak.types.structs.LuaL_Reg
import me.zombii.luak.util.LuaState
import kotlin.math.sin

object Test {

    @JvmStatic
    fun main(args: Array<String>) {
        val nativeAPI = LuaK.nativeAPI

        val state: LuaState = nativeAPI.luaL_newstate()
        nativeAPI.luaL_openlibs(state)

        val staticObj: StaticObject<HelloWorld> = ClassConverter.process_class(HelloWorld::class.java)
        staticObj.register(state)

        loadTestLib(state)

        nativeAPI.luaL_dofile(state, "test.lua")

        nativeAPI.lua_close(state)
    }

    @JvmStatic
    fun loadTestLib(state: LuaState) {
        val nativeAPI = LuaK.nativeAPI

        nativeAPI.luaL_requiref(state, "_Q", Lua_CFunction { luaState: LuaState ->
            val runtime = (nativeAPI as LoadedLibrary).getRuntime()
            val sin = LuaL_Reg(runtime)
            sin.setName("sin")
            sin.setFunc sin@{ luaState1: LuaState ->
                val s: Double = nativeAPI.lua_tonumber(luaState1, 1)
                nativeAPI.lua_pop(luaState1, 1)
                nativeAPI.lua_pushnumber(luaState1, sin(s))
                return@sin 1
            }

            nativeAPI.lua_pushglobaltable(luaState)
            nativeAPI.luaL_setfuncs_impl(luaState, Struct.getMemory(sin), 0)

            nativeAPI.lua_pushnumber(luaState, 1234.0)
            nativeAPI.lua_setfield(luaState, -2, "Test_Value")
            1
        }, 0)
    }

}