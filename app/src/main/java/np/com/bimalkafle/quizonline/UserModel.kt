package np.com.bimalkafle.quizonline

data class UserModel(
    val uid: String = "",
    val email: String = "",
    val role: String = "student"
) {
    constructor() : this("", "", "student")
}