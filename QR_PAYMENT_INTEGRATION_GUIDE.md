# QR Payment System Integration Guide

## Overview
This document describes the implementation of a QR payment system with real-time WebSocket notifications for your e-commerce platform. The system allows users to pay for orders by scanning QR codes and provides real-time status updates.

## Architecture Flow

### 1. Order Creation Flow
```
Frontend (Next.js) → Backend API → Database
                                → QR Code Generation
                                → Cloudinary Upload
                                → WebSocket Ready
```

### 2. Payment Processing Flow
```
User Scans QR → Banking App → SePay Webhook → Backend
                                            → Database Update
                                            → WebSocket Notification
                                            → Frontend Update
                                            → Auto Redirect
```

## Backend Implementation

### Files Created/Modified:

1. **WebSocketConfig.java** - WebSocket configuration
2. **OrderStatusNotificationService.java** - WebSocket notification service
3. **OrderStatusMessage.java** - DTO for WebSocket messages
4. **SePayWebHookService.java** - Updated with WebSocket notifications
5. **OrderService.java** - Enhanced with QR code generation
6. **OrderController.java** - Fixed duplicate mapping issue

### Key API Endpoints:

#### Start QR Payment Order
```http
PATCH /order/start
Content-Type: application/json

{
    "orderId": 123,
    "addressId": 456,
    "isQR": true,
    "note": "Please deliver safely"
}
```

**Response:**
```json
{
    "result": {
        "orderId": 123,
        "status": "paying",
        "QRCode": "https://res.cloudinary.com/your-cloud/image/upload/v1234567890/qr-code-url.png",
        // ... other order details
    }
}
```

#### Payment Webhook (SePay)
```http
POST /hooks/sepay-payment
Content-Type: application/json

{
    "content": "ABC12345",
    "transferAmount": 100000
}
```

### WebSocket Endpoints:

- **Connection:** `/ws` (with SockJS fallback)
- **General Updates:** `/topic/order-status`
- **User-specific Updates:** `/topic/order-status/{userId}`

## Frontend Implementation (Next.js)

### 1. Install WebSocket Dependencies
```bash
npm install sockjs-client @stomp/stompjs
```

### 2. Create WebSocket Service

```typescript
// services/websocket.ts
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

interface OrderStatusUpdate {
    orderId: number;
    qrCode: string;
    status: string;
    message: string;
    timestamp: string;
    userId: string;
}

class WebSocketService {
    private stompClient: Client | null = null;
    private isConnected = false;

    connect(): Promise<void> {
        return new Promise((resolve, reject) => {
            const socket = new SockJS(`${process.env.NEXT_PUBLIC_API_BASE_URL}/ws`);
            this.stompClient = new Client({
                webSocketFactory: () => socket,
                onConnect: () => {
                    console.log('WebSocket connected');
                    this.isConnected = true;
                    resolve();
                },
                onStompError: (frame) => {
                    console.error('WebSocket error:', frame);
                    reject(frame);
                },
            });

            this.stompClient.activate();
        });
    }

    subscribeToOrderStatus(userId: string, callback: (update: OrderStatusUpdate) => void) {
        if (!this.stompClient || !this.isConnected) {
            throw new Error('WebSocket not connected');
        }

        return this.stompClient.subscribe(`/topic/order-status/${userId}`, (message) => {
            const update: OrderStatusUpdate = JSON.parse(message.body);
            callback(update);
        });
    }

    disconnect() {
        if (this.stompClient) {
            this.stompClient.deactivate();
            this.isConnected = false;
        }
    }
}

export const websocketService = new WebSocketService();
```

### 3. QR Payment Component

```tsx
// components/QRPayment.tsx
import React, { useEffect, useState } from 'react';
import { websocketService } from '../services/websocket';
import { useRouter } from 'next/router';

interface QRPaymentProps {
    orderId: number;
    userId: string;
    qrCodeUrl: string;
}

export const QRPayment: React.FC<QRPaymentProps> = ({ orderId, userId, qrCodeUrl }) => {
    const [paymentStatus, setPaymentStatus] = useState<string>('paying');
    const [statusMessage, setStatusMessage] = useState<string>('Please scan the QR code to complete payment');
    const router = useRouter();

    useEffect(() => {
        // Connect to WebSocket
        websocketService.connect().then(() => {
            // Subscribe to order status updates
            const subscription = websocketService.subscribeToOrderStatus(userId, (update) => {
                console.log('Order status update:', update);
                
                if (update.orderId === orderId) {
                    setPaymentStatus(update.status);
                    setStatusMessage(update.message);
                    
                    // Redirect to success page when payment is completed
                    if (update.status === 'pending' || update.status === 'paid') {
                        setTimeout(() => {
                            router.push('/payment-success?orderId=' + orderId);
                        }, 2000);
                    }
                }
            });

            // Cleanup subscription on component unmount
            return () => {
                subscription.unsubscribe();
                websocketService.disconnect();
            };
        }).catch((error) => {
            console.error('Failed to connect to WebSocket:', error);
        });
    }, [orderId, userId, router]);

    const getStatusColor = (status: string) => {
        switch (status) {
            case 'paying': return 'bg-yellow-100 border-yellow-300';
            case 'pending': return 'bg-blue-100 border-blue-300';
            case 'paid': return 'bg-green-100 border-green-300';
            default: return 'bg-gray-100 border-gray-300';
        }
    };

    return (
        <div className="max-w-md mx-auto p-6">
            <div className={`p-4 rounded-lg border-2 ${getStatusColor(paymentStatus)} mb-6`}>
                <h3 className="text-lg font-semibold mb-2">
                    Payment Status: {paymentStatus.charAt(0).toUpperCase() + paymentStatus.slice(1)}
                </h3>
                <p className="text-gray-700">{statusMessage}</p>
            </div>

            {paymentStatus === 'paying' && (
                <div className="text-center">
                    <img 
                        src={qrCodeUrl} 
                        alt="QR Code for Payment" 
                        className="max-w-xs mx-auto border border-gray-300 p-4 rounded-lg mb-4"
                    />
                    <p className="text-gray-600">Scan this QR code with your banking app</p>
                </div>
            )}

            {(paymentStatus === 'pending' || paymentStatus === 'paid') && (
                <div className="text-center">
                    <div className="text-green-500 text-6xl mb-4">✓</div>
                    <p className="text-lg font-semibold text-green-700">Payment Successful!</p>
                    <p className="text-gray-600">Redirecting to confirmation page...</p>
                </div>
            )}
        </div>
    );
};
```

### 4. Checkout Page Integration

```tsx
// pages/checkout.tsx
import { useState } from 'react';
import { QRPayment } from '../components/QRPayment';
import { orderApi } from '../services/api';

const CheckoutPage = () => {
    const [orderData, setOrderData] = useState(null);
    const [paymentMethod, setPaymentMethod] = useState<'COD' | 'QR'>('COD');
    const [showQRPayment, setShowQRPayment] = useState(false);

    const handleCreateOrder = async () => {
        try {
            // First create the order
            const createResponse = await orderApi.createOrder({
                items: cartItems,
                paymentMethod: paymentMethod,
                notes: orderNotes,
                fromCart: true
            });

            const orderId = createResponse.data.result.orderId;

            // If QR payment is selected, start the QR payment process
            if (paymentMethod === 'QR') {
                const startResponse = await orderApi.startOrder({
                    orderId: orderId,
                    addressId: selectedAddressId,
                    isQR: true,
                    note: orderNotes
                });

                setOrderData(startResponse.data.result);
                setShowQRPayment(true);
            } else {
                // Handle COD order completion
                await orderApi.startOrder({
                    orderId: orderId,
                    addressId: selectedAddressId,
                    isQR: false,
                    note: orderNotes
                });
                
                // Redirect to order confirmation
                router.push('/order-confirmation?orderId=' + orderId);
            }
        } catch (error) {
            console.error('Order creation failed:', error);
        }
    };

    if (showQRPayment && orderData) {
        return (
            <QRPayment
                orderId={orderData.orderId}
                userId={currentUser.id}
                qrCodeUrl={orderData.QRCode}
            />
        );
    }

    return (
        <div>
            {/* Your existing checkout form */}
            <div className="payment-methods">
                <label>
                    <input 
                        type="radio" 
                        value="COD" 
                        checked={paymentMethod === 'COD'}
                        onChange={(e) => setPaymentMethod(e.target.value as 'COD')}
                    />
                    Cash on Delivery
                </label>
                <label>
                    <input 
                        type="radio" 
                        value="QR" 
                        checked={paymentMethod === 'QR'}
                        onChange={(e) => setPaymentMethod(e.target.value as 'QR')}
                    />
                    QR Code Payment
                </label>
            </div>
            
            <button onClick={handleCreateOrder} className="bg-blue-500 text-white px-6 py-3 rounded">
                {paymentMethod === 'QR' ? 'Generate QR Payment' : 'Place Order'}
            </button>
        </div>
    );
};
```

## Testing the Implementation

### 1. Start Your Spring Boot Application
```bash
./mvnw spring-boot:run
```

### 2. Test WebSocket Connection
Open the example HTML file: `http://localhost:8080/qr-payment-example.html`

### 3. Test the Complete Flow
1. Create an order with `isQR: true`
2. Verify QR code generation and Cloudinary upload
3. Simulate payment via webhook
4. Verify WebSocket notifications
5. Check automatic status updates

## Environment Variables

Add these to your `.env` files:

### Backend (application.yml)
```yaml
websocket:
  allowed-origins: ${FRONTEND_URL:http://localhost:3000}
```

### Frontend (.env.local)
```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
NEXT_PUBLIC_WS_URL=http://localhost:8080/ws
```

## Security Considerations

1. **WebSocket Authentication**: Add user authentication to WebSocket connections
2. **Origin Validation**: Configure allowed origins in production
3. **Rate Limiting**: Implement rate limiting for webhook endpoints
4. **Webhook Validation**: Add signature verification for SePay webhooks

## Deployment Notes

### Production Configuration
```yaml
# application-prod.yml
websocket:
  allowed-origins: 
    - https://yourdomain.com
    - https://www.yourdomain.com
```

### Load Balancer Configuration
If using multiple backend instances, ensure session stickiness for WebSocket connections or use Redis for session sharing.

## Troubleshooting

### Common Issues:
1. **WebSocket Connection Failed**: Check CORS configuration
2. **QR Code Not Generated**: Verify Cloudinary credentials
3. **Webhook Not Received**: Check SePay webhook URL configuration
4. **Messages Not Received**: Verify WebSocket subscription topics

### Debug Mode:
Add to application.yml:
```yaml
logging:
  level:
    com.tawn.tawnht.service.OrderStatusNotificationService: DEBUG
    org.springframework.web.socket: DEBUG
```

## Conclusion

This QR payment system provides:
- ✅ Real-time payment status updates
- ✅ Automatic redirection after payment
- ✅ Seamless user experience
- ✅ Webhook integration with SePay
- ✅ Scalable WebSocket architecture

The system is now ready for integration with your Next.js frontend!
