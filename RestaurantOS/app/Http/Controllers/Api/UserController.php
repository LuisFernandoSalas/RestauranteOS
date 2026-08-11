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
     * Ver lista de empleados registrados
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
     * Registrar nuevo empleado
     * Ruta: POST /api/empleados
     */
    public function store(Request $request)
    {
        if (!$request->user()->isAdmin()) {
            return response()->json(['message' => 'Acceso denegado.'], 403);
        }

        $request->validate([
            'name'     => 'required|string|max:255',
            'username' => 'required|string|max:255|unique:users,username',
            'role'     => 'required|in:mesero,cocinero,cajero,admin', // Corrección: 'cocinero'
            'password' => 'required|string|min:6' // Quita '|confirmed' si no mandas confirmación desde el UI 
        ], [
            'username.unique'    => 'Este nombre de usuario ya está registrado.',
            'password.confirmed' => 'La confirmación de la contraseña no coincide.',
            'role.in'            => 'El rol seleccionado no es válido.'
        ]);

        try {
            // Estandarizamos 'cocina' a 'cocinero' para que coincida con isCocinero() en el modelo
            $role = ($request->role === 'cocina') ? 'cocinero' : $request->role;

            $empleado = User::create([
                'name'     => $request->name,
                'username' => $request->username,
                'role'     => $role,
                'password' => Hash::make($request->password),
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
     * Mostrar detalle de un empleado específico
     * Ruta: GET /api/empleados/{id}
     */
    public function show(Request $request, $id)
    {
        if (!$request->user()->isAdmin()) {
            return response()->json(['message' => 'Acceso denegado.'], 403);
        }

        $empleado = User::find($id);
        if (!$empleado) {
            return response()->json(['message' => 'Empleado no encontrado'], 404);
        }

        return response()->json($empleado, 200);
    }

    /**
     * Actualizar datos de un empleado
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

        $request->validate([
            'name'     => 'sometimes|required|string|max:255',
            'username' => 'sometimes|required|string|max:255|unique:users,username,' . $id,
            'role'     => 'sometimes|required|in:mesero,cocinero,cajero,admin', // Corrección: 'cocinero'
            'password' => 'nullable|string|min:6'
        ]);

        $dataUpdate = $request->only(['name', 'username']);

        if ($request->filled('role')) {
            $dataUpdate['role'] = ($request->role === 'cocina') ? 'cocinero' : $request->role;
        }

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
     * Eliminar empleado
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

        // Evitar que el administrador se elimine a sí mismo
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