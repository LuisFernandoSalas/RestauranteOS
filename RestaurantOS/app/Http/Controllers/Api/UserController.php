<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Log;

class UserController extends Controller
{
    /**
     * Ver lista de empleados registrados (Tabla inferior de la UI)
     * Ruta: GET /api/empleados
     */
    public function index(Request $request)
    {
        // Filtro de seguridad: Solo admin puede ver todo el personal
        if (!$request->user()->isAdmin()) {
            return response()->json(['message' => 'Acceso denegado. Solo administradores.'], 403);
        }

        // Traemos a todos los usuarios ordenados por los más recientes
        $empleados = User::orderBy('created_at', 'desc')->get();
        
        return response()->json($empleados, 200);
    }

    /**
     * Registrar nuevo empleado (Formulario superior de la UI)
     * Ruta: POST /api/empleados
     */
    public function store(Request $request)
    {
        if (!$request->user()->isAdmin()) {
            return response()->json(['message' => 'Acceso denegado.'], 403);
        }

        // La validación exige que el username sea único en MySQL
        // 'confirmed' asume que Java enviará 'password' y 'password_confirmation'
        $request->validate([
            'name'     => 'required|string|max:255',
            'username' => 'required|string|max:255|unique:users,username',
            'role'     => 'required|in:mesero,cocina,cajero,admin',
            'password' => 'required|string|min:6|confirmed' 
        ]);

        try {
            $empleado = User::create([
                'name'     => $request->name,
                'username' => $request->username,
                'role'     => $request->role,
                'password' => Hash::make($request->password), // Encriptación obligatoria
            ]);

            return response()->json([
                'status'  => 'success',
                'message' => 'Empleado registrado exitosamente.',
                'data'    => $empleado
            ], 201);

        } catch (\Exception $e) {
            Log::error('Error al registrar empleado: ' . $e->getMessage());
            return response()->json(['error' => 'Error interno al registrar empleado'], 500);
        }
    }

    /**
     * Actualizar datos de un empleado (Botón "Editar" en la UI)
     * Ruta: PUT /api/empleados/{id}
     */
    public function update(Request $request, $id)
    {
        if (!$request->user()->isAdmin()) {
            return response()->json(['message' => 'Acceso denegado.'], 403);
        }

        $empleado = User::find($id);
        if (!$empleado) {
            return response()->json(['message' => 'Empleado no encontrado'], 404);
        }

        // Ignoramos el unique username si es el mismo usuario que estamos editando
        $request->validate([
            'name'     => 'sometimes|required|string|max:255',
            'username' => 'sometimes|required|string|max:255|unique:users,username,' . $id,
            'role'     => 'sometimes|required|in:mesero,cocina,cajero,admin',
            'password' => 'nullable|string|min:6|confirmed'
        ]);

        $dataUpdate = $request->only(['name', 'username', 'role']);
        
        // Solo actualizamos la contraseña si el administrador escribió una nueva
        if ($request->filled('password')) {
            $dataUpdate['password'] = Hash::make($request->password);
        }

        $empleado->update($dataUpdate);

        return response()->json([
            'status'  => 'success',
            'message' => 'Datos del empleado actualizados.',
            'data'    => $empleado
        ], 200);
    }

    /**
     * Eliminar empleado (Botón "Eliminar" en la UI)
     * Ruta: DELETE /api/empleados/{id}
     */
    public function destroy(Request $request, $id)
    {
        if (!$request->user()->isAdmin()) {
            return response()->json(['message' => 'Acceso denegado.'], 403);
        }

        $empleado = User::find($id);
        if (!$empleado) {
            return response()->json(['message' => 'Empleado no encontrado'], 404);
        }

        // Evitar que el administrador se elimine a sí mismo por accidente
        if ($request->user()->id == $id) {
            return response()->json(['message' => 'No puedes eliminar tu propia cuenta de administrador.'], 400);
        }

        $empleado->delete();

        return response()->json([
            'status'  => 'success',
            'message' => 'Empleado eliminado del sistema.'
        ], 200);
    }
}