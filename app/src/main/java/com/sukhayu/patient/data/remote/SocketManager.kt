// package com.sukhayu.patient.data.remote

// import android.util.Log
// import io.socket.client.IO
// import io.socket.client.Socket

// object SocketManager {

//     private var socket: Socket? = null

//     fun connect() {
//         try {
//             socket = IO.socket("https://api.sukhayu.in/")
//             socket?.connect()
//             Log.d("SocketManager", "Connected to server")
//         } catch (e: Exception) {
//             Log.e("SocketManager", "Error: ${e.message}")
//         }
//     }

//     fun emitEvent(event: String, data: Any) {
//         socket?.emit(event, data)
//     }

//     fun onEvent(event: String, callback: (Any) -> Unit) {
//         socket?.on(event) { args ->
//             if (args.isNotEmpty()) callback(args[0])
//         }
//     }

//     fun disconnect() {
//         socket?.disconnect()
//         socket = null
//     }
// }
