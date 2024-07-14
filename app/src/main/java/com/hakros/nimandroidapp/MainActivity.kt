package com.hakros.nimandroidapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.hakros.nimandroidapp.ui.theme.NimAndroidAppTheme
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Python
        Python.start(AndroidPlatform(this))

        val py = Python.getInstance()
        val gameModule: PyObject = py.getModule("nim")
        val ai: PyObject = gameModule.callAttr("train", 10000)
        var game: PyObject = gameModule.callAttr("Nim")

        setContent {
            NimAndroidAppTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp) // Optional padding
                ) {
                    // Initialize state for play variable
                    var gameCount by remember {
                        mutableIntStateOf(0)
                    }
                    var player by remember { mutableStateOf("human") }
                    var userPile by remember {
                        mutableStateOf("")
                    }
                    var userCount by remember {
                        mutableStateOf("")
                    }
                    var aiLog by remember {
                        mutableStateOf<String?>(null)
                    }

                    val winner = game.getValue("winner")?.toString()

                    if (aiLog != null) {
                        Text("$aiLog")
                    }

                    Text("Piles:")
                    for ((index, pile) in game.getValue("piles").asList().withIndex()) {
                        val sticks = drawSticks(pile.toInt())

                        Button(
                            onClick = {
                                userPile = "$index"
                            }
                        ) {
                            var chosenPile: Int = -1

                            if (userPile != "") {
                                chosenPile = userPile.toInt()
                            }

                            // Representation of a pile
                            DrawPile(index, chosenPile, sticks)
                        }
                    }

                    if (winner != null) {
                        Text("Winner is $player")

                        Button(onClick = {
                            game = gameModule.callAttr("Nim")
                            gameCount += 1
                            player = "human"
                            userPile = ""
                            userCount = ""
                            aiLog = null
                        }) {
                            Text("Reset")
                        }
                    } else if (player === "human") {
                        TextField(
                            value = userCount,
                            onValueChange = { userCount = it },
                            label = { Text("Choose Count") }
                        )

                        Button(onClick = {
                            // Process the input
                            game.callAttr("move", userPile.toInt(), userCount.toInt())
                            player = "ai"
                        }) {
                            Text("Submit")
                        }
                    } else {
                        Text(text = "AI's Turn")

                        // Get the AI's action
                        val move = ai.callAttr("choose_action", game.getValue("piles"), false).asList()
                        val pile = move[0].toInt()
                        val count = move[1].toInt()

                        game.callAttr("move", pile, count)

                        // Display the action chosen by the AI
                        aiLog = "AI chose to take $count from pile $pile."

                        player = "human"
                    }
                }
            }
        }
    }

    /**
     * Draws a graphical or string representation of a group of sticks
     *
     * @param numSticks The number of sticks in the group
     */
    private fun drawSticks(numSticks: Int): String {
        var sticks = ""

        for (i in 1..numSticks step 1) {
            if (i > 5 && (i % 5) == 1) {
                sticks += "-"
            }

            sticks += "|"
        }

        return sticks
    }

    /**
     * Draws a graphical or string representation of a pil
     *
     * @param pileNum Represents what number of pile this is
     * @param chosenPile The pile that the user chose to change
     * @param sticks graphical or string representation of the number of sticks remaining in the pile
     */
    @Composable
    private fun DrawPile(pileNum: Int, chosenPile: Int, sticks: String) {
        var text = "O   Pile $pileNum: $sticks"

        if (chosenPile > -1 && chosenPile == pileNum) {
            text = "X   Pile $pileNum: $sticks"
        }

        return Text(text)
    }
}