<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;

class AuthController extends Controller
{
    //iniciar sesion(generar token)
    //ruta: POST /api/login
    public function login(Request $request)
    {
        /**
         * validarque java nos mande los datos correctamente
         */
        $request->validate([
            'username' => 'required|string',
            'password' => 'required|string'
        ]);
        //buscar el empleado en mysql por su username
        $user = User::where('username', $request->username)->first();
        //si no existe o la contraseña no coincide, retornar error
        if (!$user || !Hash::check($request->password, $user->password)) {
            return response()->json([
                'status'  => 'error',
                'message' => 'Credenciales inválidas'
            ], 401);
        }
        //si todo sale bien, generamos el token de seguridad(sanctum)
        //le ponemos un nombre descriptivo al token
        $token = $user->createToken('restaurantos-java-app')->plainTextToken;
        //devolver la respuesta a la app java
        return response()->json([
            'status'       => 'success',
            'message'      => 'Inicio de sesión exitoso',
            'access_token' => $token,
            'token_type'   => 'Bearer',//estandar de api rest
             'user'        => [
                'id'       => $user->id,
                'name'     => $user->name,
                'role'     => $user->role,//para saber que pantalla mostrar
                'username' => $user->username,
            ]
        ], 200);     
    }
    /**
     * cerrar sesion (Destruir token)
     * ruta: POST /api/logout
     */
    public function logout(Request $request)
    {
        //revocar/eliminar el token que el usuario esta usando en este momento en mysql
        $request->user()->currentAccessToken()->delete();
        return response()->json([
            'status'  => 'success',
            'message' => 'Cierre de sesión exitoso'
        ], 200);
    }
}

