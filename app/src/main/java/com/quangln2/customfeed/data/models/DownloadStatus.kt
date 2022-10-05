package com.quangln2.customfeed.data.models

enum class DownloadStatus {
    NONE {
         override fun toString(): String {
             return "NONE"
         }
         },
    DOWNLOADING {
        override fun toString(): String {
            return "Downloading"
        }
    },
    PAUSED {
        override fun toString(): String {
            return "Paused"
        }
    },
    COMPLETED {
        override fun toString(): String {
            return "Completed"
        }
    },
    FAILED {
        override fun toString(): String {
            return "Failed"
        }
    },
    QUEUED {
        override fun toString(): String {
            return "Queued"
        }
    }
}