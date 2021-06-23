package com.wzl.guitarpanelview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.wzl.guitarpanelview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startBtn.setOnClickListener {
            startScrollPanel()
        }

        binding.stopBtn.setOnClickListener {
            stopScrollPanel()
        }
    }

    /**
     * 滚动吉他面板
     */
    fun startScrollPanel() {
        val totalDuration = binding.guitarFretBoardView.getTotalDuration() ?: return
        binding.guitarFretBoardView.smoothScrollBy(totalDuration, 0, totalDuration)
    }

    /**
     * 停止滚动吉他面板
     */
    fun stopScrollPanel() {
        binding.guitarFretBoardView.resetScroll()
        // 清空和弦信息
//        chordDiagramsView?.reset()
    }
}