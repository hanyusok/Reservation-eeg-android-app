package com.example.reservation_eeg_android_app.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class PhoneNumberTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 2 || i == 6) out += "-"
        }

        val phoneNumberOffsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 3) return offset
                if (offset <= 7) return offset + 1
                return offset + 2
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 3) return offset
                if (offset <= 8) return offset - 1
                return offset - 2
            }
        }

        return TransformedText(AnnotatedString(out), phoneNumberOffsetMapping)
    }
}

class ResidentIdTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 5) out += "-"
        }

        val residentIdOffsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 6) return offset
                return offset + 1
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 6) return offset
                return offset - 1
            }
        }

        return TransformedText(AnnotatedString(out), residentIdOffsetMapping)
    }
}
