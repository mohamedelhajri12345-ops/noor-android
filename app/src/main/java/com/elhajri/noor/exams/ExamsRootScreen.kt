package com.elhajri.noor.exams
import com.elhajri.noor.settings.t

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

sealed class ExamScreenState {
    object Map : ExamScreenState()
    data class Playing(val track: ExamTrack, val difficulty: ExamDifficulty, val levelNumber: Int) : ExamScreenState()
    data class Result(val examResult: ExamResult) : ExamScreenState()
    data class Certificate(val track: ExamTrack, val difficulty: ExamDifficulty) : ExamScreenState()
}

@Composable
fun ExamsRootScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Initialize Question Bank
    LaunchedEffect(Unit) {
        ExamQuestionBank.loadBank(context)
    }

    var currentState by remember { mutableStateOf<ExamScreenState>(ExamScreenState.Map) }

    when (val state = currentState) {
        is ExamScreenState.Map -> {
            ExamMapScreen(
                onSelectLevel = { track, diff, levelNum ->
                    currentState = ExamScreenState.Playing(track, diff, levelNum)
                },
                onOpenCertificate = { track, diff ->
                    currentState = ExamScreenState.Certificate(track, diff)
                },
                onBack = onBack
            )
        }

        is ExamScreenState.Playing -> {
            ExamPlayScreen(
                track = state.track,
                difficulty = state.difficulty,
                levelNumber = state.levelNumber,
                onFinishExam = { result ->
                    currentState = ExamScreenState.Result(result)
                },
                onBack = {
                    currentState = ExamScreenState.Map
                }
            )
        }

        is ExamScreenState.Result -> {
            ExamResultScreen(
                result = state.examResult,
                onRetake = {
                    currentState = ExamScreenState.Playing(
                        state.examResult.track,
                        state.examResult.difficulty,
                        state.examResult.levelNumber
                    )
                },
                onNextLevel = {
                    currentState = ExamScreenState.Playing(
                        state.examResult.track,
                        state.examResult.difficulty,
                        minOf(state.examResult.levelNumber + 1, state.examResult.difficulty.totalLevels)
                    )
                },
                onOpenCertificate = {
                    currentState = ExamScreenState.Certificate(
                        state.examResult.track,
                        state.examResult.difficulty
                    )
                },
                onBackToMap = {
                    currentState = ExamScreenState.Map
                }
            )
        }

        is ExamScreenState.Certificate -> {
            ExamCertificateScreen(
                track = state.track,
                difficulty = state.difficulty,
                onBack = {
                    currentState = ExamScreenState.Map
                }
            )
        }
    }
}
