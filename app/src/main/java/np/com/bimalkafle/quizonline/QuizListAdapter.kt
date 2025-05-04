package np.com.bimalkafle.quizonline

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import np.com.bimalkafle.quizonline.databinding.QuizItemRecyclerRowBinding

class QuizListAdapter(
    private val quizModelList: List<QuizModel>,
    private val onDeleteClick: ((QuizModel) -> Unit)? = null,
    private val onEditClick: ((QuizModel) -> Unit)? = null // New callback for edit
) : RecyclerView.Adapter<QuizListAdapter.MyViewHolder>() {

    class MyViewHolder(private val binding: QuizItemRecyclerRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(model: QuizModel, onDeleteClick: ((QuizModel) -> Unit)?, onEditClick: ((QuizModel) -> Unit)?) {
            binding.apply {
                quizTitleText.text = model.title
                quizSubtitleText.text = model.subtitle
                quizTimeText.text = root.context.getString(R.string.time_format, model.time)

                if (onDeleteClick != null) {
                    // Show delete button only for teachers
                    deleteBtn.visibility = View.VISIBLE
                    deleteBtn.setOnClickListener { onDeleteClick(model) }
                } else {
                    deleteBtn.visibility = View.GONE
                }

                if (onEditClick != null) {
                    // Show edit button only for teachers
                    editBtn.visibility = View.VISIBLE
                    editBtn.setOnClickListener { onEditClick(model) }
                } else {
                    editBtn.visibility = View.GONE
                }

                root.setOnClickListener {
                    val intent = Intent(root.context, QuizActivity::class.java).apply {
                        putExtra("QUIZ_ID", model.id)
                        putExtra("START_TIME", model.startTime)
                        putExtra("END_TIME", model.endTime)
                    }
                    QuizActivity.questionModelList = model.questionList
                    QuizActivity.time = model.time
                    QuizActivity.startTime = model.startTime
                    QuizActivity.endTime = model.endTime
                    root.context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = QuizItemRecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return quizModelList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(quizModelList[position], onDeleteClick, onEditClick)
    }
}