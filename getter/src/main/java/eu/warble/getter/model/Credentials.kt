package eu.warble.getter.model

data class Credentials(val login: String, val password: String) {

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Credentials) return false
        if (other.login == this.login && other.password == this.password) return true
        return false
    }

    override fun hashCode(): Int {
        var result = login.hashCode()
        result = 31 * result + password.hashCode()
        return result
    }

}