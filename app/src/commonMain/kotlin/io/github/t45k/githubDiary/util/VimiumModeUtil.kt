package io.github.t45k.githubDiary.util

import androidx.compose.ui.input.key.Key
import kotlinx.datetime.LocalDate

private val dayToChar = ('0'..'9') + // add '0' for offset
    ('A'..'Z').filterNot { it == 'F' } // 'F' is duplicated with Vimium key bind

private val keyToChar = mapOf(
    Key.One to '1',
    Key.Two to '2',
    Key.Three to '3',
    Key.Four to '4',
    Key.Five to '5',
    Key.Six to '6',
    Key.Seven to '7',
    Key.Eight to '8',
    Key.Nine to '9',
    Key.A to 'A',
    Key.B to 'B',
    Key.C to 'C',
    Key.D to 'D',
    Key.E to 'E',
    Key.G to 'G',
    Key.H to 'H',
    Key.I to 'I',
    Key.J to 'J',
    Key.K to 'K',
    Key.L to 'L',
    Key.M to 'M',
    Key.N to 'N',
    Key.O to 'O',
    Key.P to 'P',
    Key.Q to 'Q',
    Key.R to 'R',
    Key.S to 'S',
    Key.T to 'T',
    Key.U to 'U',
    Key.V to 'V',
    Key.W to 'W',
    Key.X to 'X',
    Key.Y to 'Y',
    Key.Z to 'Z',
)

fun LocalDate.toVimiumChar(): String = dayToChar[day].toString()

fun Key.isApplicableForVimium(): Boolean = this in keyToChar

fun Key.toVimiumDay(): Int =
    keyToChar[this]?.let { char ->
        dayToChar.indexOf(char)
            .takeIf { it >= 1 }
    } ?: error("Day for key $this not found")
