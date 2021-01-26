package com.alperenbabagil.taggylib

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.alperenbabagil.commonextensions.hide
import com.alperenbabagil.commonextensions.show
import com.alperenbabagil.commonextensions.toPx
import com.alperenbabagil.taggylib.databinding.SingleTagLayoutBinding
import com.alperenbabagil.taggylib.databinding.TagLayoutBinding
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.progressindicator.CircularDrawingDelegate
import com.google.android.material.progressindicator.CircularIndeterminateAnimatorDelegate
import com.google.android.material.progressindicator.IndeterminateDrawable
import com.google.android.material.progressindicator.ProgressIndicatorSpec
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TagView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
        defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyle, defStyleRes) {

    private var mainViewBinding: TagLayoutBinding =
        TagLayoutBinding.inflate(LayoutInflater.from(context), this)

    private val selectedTags = mutableListOf<String>()
    private val suggestedTags = mutableListOf<String>()

    var cancelButtonCallback: (() -> Unit)? = null
    var doneButtonCallback: ((selectedTags: List<String>) -> Unit)? = null
    var onTagSelectedCallback: ((selectedTags: List<String>) -> Unit)? = null
    var onTagCancelledCallback: ((tag: String) -> Unit)? = null
    var maxSelectedTagsReachedCallback: ((selectedTags: List<String>) -> Unit)? = null
    var textEnteredCallback: ((newText: String) -> Unit)? = null

    fun getSelectedTags() = selectedTags.distinct()
    fun getSuggestedTags() = suggestedTags.distinct()

    private var showDoneButton = true
    private var showCancelButton = true
    var selectedTagsLimit = 5
    var suggestedTagsLimit = 5
    private var showMaxSelectedTagsWarning = true
    private var maxSelectedTagsWarningText = context.getString(R.string.max_selected_tab_number_is_reached)
    private var maxSelectedTagsWarningDuration = 3000
    private var warningTextTimeoutInMilliseconds = 5000
    private var currentHideToastJob : Job?=null

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it,
                    R.styleable.tag_view_item_attributes, 0, 0)

            showDoneButton = typedArray.getBoolean(R.styleable.tag_view_item_attributes_show_done_button, true)
            showCancelButton = typedArray.getBoolean(R.styleable.tag_view_item_attributes_show_cancel_button, true)
            selectedTagsLimit = typedArray.getInt(R.styleable.tag_view_item_attributes_selected_tags_limit, selectedTagsLimit)
            suggestedTagsLimit = typedArray.getInt(R.styleable.tag_view_item_attributes_suggested_tags_limit, suggestedTagsLimit)
            showMaxSelectedTagsWarning = typedArray.getBoolean(R.styleable.tag_view_item_attributes_show_max_selected_tags_warning, true)
            maxSelectedTagsWarningText = typedArray.getString(R.styleable.tag_view_item_attributes_max_selected_tags_warning_text) ?: maxSelectedTagsWarningText
            maxSelectedTagsWarningDuration = typedArray.getInt(R.styleable.tag_view_item_attributes_max_selected_tags_warning_duration, maxSelectedTagsWarningDuration)
            warningTextTimeoutInMilliseconds = typedArray.getInt(R.styleable.tag_view_item_attributes_warning_text_timeout_in_milliseconds, warningTextTimeoutInMilliseconds)
            typedArray.recycle()
        }

        mainViewBinding.apply {
            if (showDoneButton) {
                doneTagEditingBTN.apply {
                    setOnClickListener {
                        if(selectedTags.isEmpty()){
                            showWarningMessage(context.getString(R.string.you_havent_selected_any_tabs))
                        }
                        else doneButtonCallback?.invoke(getSelectedTags())
                    }
                    visibility = View.VISIBLE
                }
            }

            if (showCancelButton) {
                cancelTagEditingBTN.apply {
                    setOnClickListener {
                        cancelButtonCallback?.invoke()
                    }
                    visibility = View.VISIBLE
                }
            }

            tagEnterET.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    addTag(tagEnterET.text.toString())
                    mainViewBinding.tagEnterET.setText("")
                    return@setOnEditorActionListener true
                }
                false
            }

            tagEnterET.doOnTextChanged { text, start, before, count ->
                textEnteredCallback?.invoke(text.toString().trimEnd())
            }
        }
    }

    fun setSelectedTags(list: List<String>) {
        selectedTags.apply {
            clear()
            addAll(list.distinct().take(selectedTagsLimit))
            updateFlexBoxView(this, mainViewBinding.selectedTagsFBL, false)
        }
    }

    fun setSuggestedTags(list: List<String>) {
        suggestedTags.apply {
            clear()
            addAll(list.distinct().take(suggestedTagsLimit))
            updateFlexBoxView(this, mainViewBinding.suggestedTagsFBL, true)
        }
    }

    private fun updateFlexBoxView(list: MutableList<String>, flexBoxLayout: FlexboxLayout, isSuggested: Boolean) {
        flexBoxLayout.removeAllViews()
        list.forEach { text ->
            flexBoxLayout.addView(
                    SingleTagLayoutBinding.inflate(LayoutInflater.from(context), this, false).apply {
                        arrangeTag(list, flexBoxLayout, text, this, isSuggested)
                    }.root
            )
        }
    }

    private fun addTag(tagStr: String){
        if(selectedTags.size >= selectedTagsLimit){
            maxSelectedTagsReachedCallback?.invoke(getSelectedTags())
            if(showMaxSelectedTagsWarning){
                showWarningMessage(maxSelectedTagsWarningText,maxSelectedTagsWarningDuration)
            }
        }
        else{
            setSelectedTags(mutableListOf(tagStr).apply { addAll(selectedTags) })
            onTagSelectedCallback?.invoke(getSelectedTags())
        }
    }

    private fun arrangeTag(list: MutableList<String>, flexBoxLayout: FlexboxLayout, text: String,
                           singleTagLayoutBinding: SingleTagLayoutBinding, isSuggested: Boolean){
        singleTagLayoutBinding.apply {
            if (isSuggested) {
                root.setOnClickListener {
                    addTag(text)
                    updateFlexBoxView(list, flexBoxLayout, isSuggested)
                }
            }
            tagTV.text = text
            cancelButton.apply {
                if (isSuggested) {
                    visibility = View.GONE
                } else {
                    setOnClickListener {
                        list.removeAt(list.indexOf(text))
                        updateFlexBoxView(list, flexBoxLayout, isSuggested)
                        onTagCancelledCallback?.invoke(text)
                    }
                }
            }
        }
    }

    fun setLoadingState(isLoading:Boolean){
        mainViewBinding.tagEnterTIL.apply {
            if(isLoading){
                endIconMode = TextInputLayout.END_ICON_CUSTOM
                endIconDrawable = getLoadingIndicator()
            }
            else{
                endIconMode = TextInputLayout.END_ICON_NONE
                endIconDrawable = null

            }
        }
    }

    private fun getLoadingIndicator() : IndeterminateDrawable{
        val progressIndicatorSpec = ProgressIndicatorSpec()
        progressIndicatorSpec.loadFromAttributes(
                context,
                null,
                R.style.Widget_MaterialComponents_ProgressIndicator_Circular_Indeterminate)

        progressIndicatorSpec.circularInset = 0 // Inset

        progressIndicatorSpec.circularRadius = 10.toPx()


        return IndeterminateDrawable(
                context,
                progressIndicatorSpec,
                CircularDrawingDelegate(),
                CircularIndeterminateAnimatorDelegate())
    }

    fun showWarningMessage(message:String,
                           duration: Int=warningTextTimeoutInMilliseconds,
                           id:Int=1,
                           toastClickCallback : ((id:Int,message:String) -> Unit)?=null){
        (context as? LifecycleOwner)?.let {
            currentHideToastJob?.cancel()
            mainViewBinding.warningCV.hide()
            currentHideToastJob=it.lifecycleScope.launch (Dispatchers.Main){

                mainViewBinding.apply {
                    warningCV.show()
                    warningCV.setOnClickListener {
                        toastClickCallback?.invoke(id,message)
                    }
                    warningTV.text=message
                }

                delay(duration.toLong())

                mainViewBinding.apply {
                    warningCV.hide()
                    warningCV.setOnClickListener(null)
                    warningTV.text=""
                }

            }
        }
    }
}