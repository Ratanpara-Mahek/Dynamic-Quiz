package np.com.bimalkafle.quizonline

import java.io.Serializable

data class QuizModel(
    val id: String,
    val title: String,
    val subtitle: String,
    val time: String,
    val questionList: List<QuestionModel>,
    val startTime: Long = 0L,
    val endTime: Long = 0L
) : Serializable {
    constructor() : this("", "", "", "", emptyList(), 0L, 0L)
}

data class QuestionModel(
    val question: String,
    val options: List<String>,
    val correct: String
) : Serializable {
    constructor() : this("", emptyList(), "")
}

data class QuizResult(
    val quizId: String = "",
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val percentage: Int = 0,
    val timestamp: String = ""
) : Serializable {
    constructor() : this("", 0, 0, 0, "")
}