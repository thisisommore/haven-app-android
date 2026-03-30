package com.example.haven.xxdk

fun XXDK.applyIdentity(identity: GeneratedIdentity) {
    codename = identity.codename
    codeset = identity.codeset
    savePrivateIdentity(identity.privateIdentity)
}
