<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class NotificationController extends Controller
{
    /**
     * GET /api/notificaciones
     * Lista notificaciones no leídas del usuario autenticado
     */
    public function index(Request $request): JsonResponse
    {
        $notificaciones = $request->user()
            ->unreadNotifications()
            ->latest()
            ->get();

        return response()->json([
            'status' => 'success',
            'data'   => $notificaciones
        ], 200);
    }

    /**
     * PUT /api/notificaciones/{id}/read
     * Marcar una notificación específica como leída
     */
    public function markAsRead(Request $request, string $id): JsonResponse
    {
        $notification = $request->user()
            ->notifications()
            ->where('id', $id)
            ->first();

        if ($notification) {
            $notification->markAsRead();
        }

        return response()->json([
            'status'  => 'success',
            'message' => 'Notificación marcada como leída'
        ], 200);
    }

    /**
     * PUT /api/notificaciones/read-all
     * Marcar todas las notificaciones del usuario como leídas
     */
    public function markAllAsRead(Request $request): JsonResponse
    {
        $request->user()->unreadNotifications->markAsRead();

        return response()->json([
            'status'  => 'success',
            'message' => 'Todas las notificaciones fueron marcadas como leídas'
        ], 200);
    }
}