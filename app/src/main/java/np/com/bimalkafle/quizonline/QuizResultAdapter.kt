package np.com.bimalkafle.quizonline

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import np.com.bimalkafle.quizonline.databinding.QuizResultItemBinding

class QuizResultAdapter(
    private val quizResultList: List<QuizResult>
) : RecyclerView.Adapter<QuizResultAdapter.MyViewHolder>() {

    class MyViewHolder(private val binding: QuizResultItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(result: QuizResult) {
            binding.apply {
                quizIdText.text = "Quiz ID: ${result.quizId}"
                scoreText.text = "Score: ${result.score} / ${result.totalQuestions}"
                percentageText.text = "Percentage: ${result.percentage}%"
                timestampText.text = "Date: ${result.timestamp}"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = QuizResultItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = quizResultList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(quizResultList[position])
    }
}