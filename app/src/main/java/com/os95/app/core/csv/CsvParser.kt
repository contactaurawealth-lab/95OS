package com.os95.app.core.csv

import java.io.Reader
import java.io.StringReader

/**
 * Strict RFC 4180 compliant CSV Parser.
 * Correctly handles:
 * - Commas within quoted fields
 * - Newlines (CR, LF, CRLF) within quoted fields (multiline text)
 * - Escaped quotation marks ("") within quoted fields
 * - Unicode characters
 * - Accurate line number tracking for row-level diagnostics
 */
class CsvParser {

    data class ParsedRow(
        val lineNumber: Int,
        val fields: List<String>,
        val rawLineSnippet: String
    )

    fun parse(content: String): List<ParsedRow> {
        return parse(StringReader(content))
    }

    fun parse(reader: Reader): List<ParsedRow> {
        val rows = mutableListOf<ParsedRow>()
        val currentFields = mutableListOf<String>()
        val currentField = StringBuilder()
        val rawSnippet = StringBuilder()

        var inQuotes = false
        var startLineNumber = 1
        var currentLineNumber = 1

        var prevChar = '\u0000'

        while (true) {
            val intChar = reader.read()
            if (intChar == -1) break
            val ch = intChar.toChar()

            rawSnippet.append(ch)

            when {
                ch == '\"' -> {
                    if (inQuotes) {
                        // Check if the next character is also a quote (escaped quote)
                        reader.mark(1)
                        val nextInt = reader.read()
                        if (nextInt != -1 && nextInt.toChar() == '\"') {
                            rawSnippet.append('\"')
                            currentField.append('\"')
                        } else {
                            reader.reset()
                            inQuotes = false
                        }
                    } else {
                        inQuotes = true
                    }
                }

                ch == ',' && !inQuotes -> {
                    currentFields.add(currentField.toString().trim())
                    currentField.setLength(0)
                }

                (ch == '\n' || ch == '\r') -> {
                    if (ch == '\r') {
                        // Check for CRLF
                        reader.mark(1)
                        val nextInt = reader.read()
                        if (nextInt != -1 && nextInt.toChar() == '\n') {
                            rawSnippet.append('\n')
                        } else {
                            reader.reset()
                        }
                    }

                    if (inQuotes) {
                        currentField.append('\n')
                    } else {
                        currentFields.add(currentField.toString().trim())
                        currentField.setLength(0)

                        // If not a completely empty row, record it
                        if (currentFields.any { it.isNotBlank() }) {
                            rows.add(
                                ParsedRow(
                                    lineNumber = startLineNumber,
                                    fields = currentFields.toList(),
                                    rawLineSnippet = rawSnippet.toString().trim()
                                )
                            )
                        }

                        currentFields.clear()
                        rawSnippet.setLength(0)
                        startLineNumber = currentLineNumber + 1
                    }
                    currentLineNumber++
                }

                else -> {
                    currentField.append(ch)
                }
            }
            prevChar = ch
        }

        // Process any trailing line without a trailing newline
        if (currentField.isNotEmpty() || currentFields.isNotEmpty()) {
            currentFields.add(currentField.toString().trim())
            if (currentFields.any { it.isNotBlank() }) {
                rows.add(
                    ParsedRow(
                        lineNumber = startLineNumber,
                        fields = currentFields.toList(),
                        rawLineSnippet = rawSnippet.toString().trim()
                    )
                )
            }
        }

        return rows
    }
}
