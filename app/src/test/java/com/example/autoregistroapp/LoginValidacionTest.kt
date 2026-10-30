package com.example.autoregistroapp

import com.example.autoregistroapp.data.CuentasDemo
import com.example.autoregistroapp.viewmodel.ValidadorLogin
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginValidacionTest {

    @Test
    fun usuario_vacioOCorto_esInvalido() {
        assertNotNull(ValidadorLogin.alias(""))
        assertNotNull(ValidadorLogin.alias("ab"))
        assertNull(ValidadorLogin.alias("demo"))
    }

    @Test
    fun clave_vaciaOCorta_esInvalida() {
        assertNotNull(ValidadorLogin.clave(""))
        assertNotNull(ValidadorLogin.clave("12345"))
        assertNull(ValidadorLogin.clave("Demo1234"))
    }

    @Test
    fun cuentaDemo_esValida_yUnaFalsaNo() {
        assertTrue(CuentasDemo.credencialesValidas("demo", "Demo1234"))
        assertFalse(CuentasDemo.credencialesValidas("demo", "otraClave"))
        assertFalse(CuentasDemo.credencialesValidas("nadie", "Demo1234"))
    }
}
