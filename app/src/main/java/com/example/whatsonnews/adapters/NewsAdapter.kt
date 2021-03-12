package com.example.whatsonnews.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.whatsonnews.R
import com.example.whatsonnews.news.Article
import com.example.whatsonnews.viewmodels.MainViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*

class NewsAdapter(
    private val mMainViewModel: MainViewModel,
    private val calledFromSavedNewsFragment: Boolean = false,
    private val fragmentView: View? = null
) : RecyclerView.Adapter<NewsAdapter.ArticleViewHolder>() {

    var savedUrl: List<String>? = null

    inner class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: CardView = itemView.findViewById(R.id.item_card)
        val image: ImageView = itemView.findViewById(R.id.articleImageView)
        val title: TextView = itemView.findViewById(R.id.articleTitle)
        val description: TextView = itemView.findViewById(R.id.articleDescription)
        val sourceName: TextView = itemView.findViewById(R.id.articleSource)
        val publishedAt: TextView = itemView.findViewById(R.id.articlePublishedAt)
        val fabButton: FloatingActionButton = itemView.findViewById(R.id.fabButton)
    }


    fun setSavedUrlList(t: List<String>) {
        savedUrl = t
    }

    private val differCallback = object : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.url == newItem.url
        }
        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.url == newItem.url
        }
    }

    var differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.each_article, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private var onMyArticleClickListener: ((Article) -> Unit)? = null
    fun setOnMyArticleClickListener(listener: (Article) -> Unit) {
        onMyArticleClickListener = listener
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {

        val article = differ.currentList[position]

        holder.apply {
            Glide.with(image.context).load(article.urlToImage)
                .placeholder(R.drawable.placeholder_news).into(image)
            title.text = article.title
            description.text = article.description
            sourceName.text = article.source?.name
            publishedAt.text = article.publishedAt

            val idFabIcon = if (savedUrl!!.contains(article.url!!)) R.drawable.ic_bookmark_24px else R.drawable.ic_bookmark_border_24px
            fabButton.setImageDrawable(ContextCompat.getDrawable(fabButton.context, idFabIcon))

            card.setOnClickListener {
                onMyArticleClickListener?.let { it(article) }
            }
            fabButton.setOnClickListener {
                handleSituation(article, fabButton, position)
            }
        }
    }

    private fun handleSituation(article: Article, fabButton: FloatingActionButton, position: Int) =
        runBlocking {

            val verdict = async { mMainViewModel.articleIsAlreadyPresent(article.url!!) }
            if (verdict.await() > 0) {
                // article is already saved. so un-save it.
                mMainViewModel.deleteArticle(article)
                fabButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        fabButton.context,
                        R.drawable.ic_bookmark_border_24px
                    )
                )
                //Toast.makeText(context, "Article removed. ", Toast.LENGTH_SHORT).show()
                if (calledFromSavedNewsFragment && fragmentView != null) {
                    Snackbar.make(
                        fragmentView,
                        "Article has been successfully deleted.",
                        Snackbar.LENGTH_LONG
                    )
                        .setAction("UNDO") {
                            mMainViewModel.insertArticle(article)
                        }.setActionTextColor(
                            ContextCompat.getColor(
                                fragmentView.context,
                                R.color.teal_200
                            )
                        )
                        .setTextColor(ContextCompat.getColor(fragmentView.context, R.color.white))
                        .setBackgroundTint(
                            ContextCompat.getColor(
                                fragmentView.context,
                                R.color.snack_bar_bluish_grey
                            )
                        ).show()
                }
            } else {
                // article frequency is 0 (article is not saved). so go ahead and save it.
                mMainViewModel.insertArticle(article)
                fabButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        fabButton.context,
                        R.drawable.ic_bookmark_24px
                    )
                )
                //Toast.makeText(context, "Article saved to collection.", Toast.LENGTH_SHORT).show()
            }
            val count: Int = mMainViewModel.getCountOfArticles()
            Log.e(TAG, "count = $count")
        }

    companion object {
        const val TAG = "NewsAdapter"
    }
}