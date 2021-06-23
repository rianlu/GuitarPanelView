package com.wzl.guitarpanelview

/**
Created by FaceBlack
 */
data class GuitarNote(
    // 吉他琴弦
    val stringNumberTab: Int,
    // 吉他品位
    val fretNumber: Int,
    // 时间（ms）
    var duration: Int,
    // 音符状态
    var noteState: NoteState = NoteState.NONE
) {
    override fun toString(): String {
        return "GuitarNote(stringNumberTab=$stringNumberTab, fretNumber=$fretNumber, duration=$duration, noteState=$noteState)"
    }
}

// 音符状态
enum class NoteState(val state: Int) {
    NONE(-1),
    WRONG(0),
    CORRECT(1)
}