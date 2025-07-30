package com.zhongjh.multimedia.widget.progressbutton

internal class StateManager(progressButton: CircularProgressButton) {
    private val isEnabled: Boolean
    private var progress: Int

    init {
        isEnabled = progressButton.isEnabled
        progress = progressButton.progress
    }

    fun saveProgress(progressButton: CircularProgressButton) {
        progress = progressButton.progress
    }

    fun checkState(progressButton: CircularProgressButton) {
        if (progressButton.progress != progress) {
            progressButton.progress = progressButton.progress
        } else if (progressButton.isEnabled != isEnabled) {
            progressButton.isEnabled = progressButton.isEnabled
        }
    }
}
