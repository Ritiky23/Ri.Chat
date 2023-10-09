package com.example.test1

class User {
    var name: String? = null
    var email: String? = null
    var uid: String? = null
    var friends: Map<String, Boolean>? = null
    var profilePictureUrl: String? = null

    constructor()

    constructor(name: String?, email: String?, uid: String,profilePictureUrl:String?) {
        this.name = name
        this.email = email
        this.uid = uid
    }
}
