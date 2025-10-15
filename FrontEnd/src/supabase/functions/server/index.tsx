import { Hono } from 'npm:hono';
import { cors } from 'npm:hono/cors';
import { logger } from 'npm:hono/logger';
import { createClient } from 'jsr:@supabase/supabase-js@2';
import * as kv from './kv_store.tsx';

const app = new Hono();

app.use('*', cors());
app.use('*', logger(console.log));

const supabase = createClient(
  Deno.env.get('SUPABASE_URL') ?? '',
  Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? '',
);

// Initialize admin account and sample batteries on first run
async function initializeAdmin() {
  const adminExists = await kv.get('user:admin@evswap.com');
  
  if (!adminExists) {
    const { data: adminUser, error } = await supabase.auth.admin.createUser({
      email: 'admin@evswap.com',
      password: 'Admin@123456',
      user_metadata: { 
        full_name: 'System Administrator',
        role: 'admin',
        phone: '0000000000'
      },
      email_confirm: true
    });

    if (!error && adminUser.user) {
      await kv.set(`user:${adminUser.user.id}`, {
        id: adminUser.user.id,
        email: 'admin@evswap.com',
        full_name: 'System Administrator',
        phone: '0000000000',
        role: 'admin',
        created_at: new Date().toISOString()
      });
      
      await kv.set('user:admin@evswap.com', adminUser.user.id);
      console.log('Admin account created successfully');
    }
  }
}

// Initialize sample batteries for each station
async function initializeBatteries() {
  const batteries = await kv.getByPrefix('battery:');
  
  if (batteries.length === 0) {
    const stations = await kv.getByPrefix('station:');
    
    for (const stationEntry of stations) {
      const station = stationEntry.value;
      
      // Create 10 batteries per station
      for (let i = 0; i < 10; i++) {
        const batteryId = crypto.randomUUID();
        const battery = {
          id: batteryId,
          model: i % 2 === 0 ? 'VinFast 48V-20Ah' : 'Yadea 60V-32Ah',
          soc_percent: Math.floor(Math.random() * 40) + 60, // 60-100%
          soh_percent: Math.floor(Math.random() * 10) + 90, // 90-100%
          state: Math.random() > 0.3 ? 'available' : 'charging', // 70% available, 30% charging
          station_id: station.id,
          created_at: new Date().toISOString()
        };
        
        await kv.set(`battery:${batteryId}`, battery);
      }
    }
    
    console.log('Sample batteries initialized');
  }
}

// Initialize admin and batteries on startup
initializeAdmin();
initializeBatteries();

// Sign up route for drivers
app.post('/make-server-c0c28b62/signup', async (c) => {
  try {
    const body = await c.req.json();
    const { email, password, full_name, phone } = body;

    if (!email || !password || !full_name || !phone) {
      return c.json({ error: 'All fields are required' }, 400);
    }

    // Check if user already exists
    const existingUser = await kv.get(`user:${email}`);
    if (existingUser) {
      return c.json({ error: 'User already exists' }, 400);
    }

    const { data, error } = await supabase.auth.admin.createUser({
      email,
      password,
      user_metadata: { 
        full_name,
        phone,
        role: 'driver'
      },
      email_confirm: true
    });

    if (error) {
      console.log(`Error during user signup: ${error.message}`);
      return c.json({ error: error.message }, 400);
    }

    // Save user to KV store
    await kv.set(`user:${data.user.id}`, {
      id: data.user.id,
      email,
      full_name,
      phone,
      role: 'driver',
      created_at: new Date().toISOString()
    });
    
    await kv.set(`user:${email}`, data.user.id);

    return c.json({ 
      success: true, 
      user: {
        id: data.user.id,
        email,
        full_name,
        phone,
        role: 'driver'
      }
    });
  } catch (error) {
    console.log(`Server error during signup: ${error}`);
    return c.json({ error: 'Internal server error' }, 500);
  }
});

// Login route
app.post('/make-server-c0c28b62/login', async (c) => {
  try {
    const body = await c.req.json();
    const { email, password } = body;

    const { data, error } = await supabase.auth.signInWithPassword({
      email,
      password,
    });

    if (error) {
      console.log(`Error during login: ${error.message}`);
      return c.json({ error: 'Invalid credentials' }, 401);
    }

    // Get user data from KV store
    const userData = await kv.get(`user:${data.user.id}`);

    return c.json({ 
      success: true,
      access_token: data.session.access_token,
      user: userData
    });
  } catch (error) {
    console.log(`Server error during login: ${error}`);
    return c.json({ error: 'Internal server error' }, 500);
  }
});

// Get current user
app.get('/make-server-c0c28b62/me', async (c) => {
  try {
    const accessToken = c.req.header('Authorization')?.split(' ')[1];
    if (!accessToken) {
      return c.json({ error: 'Unauthorized' }, 401);
    }

    const { data: { user }, error } = await supabase.auth.getUser(accessToken);
    if (!user || error) {
      return c.json({ error: 'Unauthorized' }, 401);
    }

    const userData = await kv.get(`user:${user.id}`);
    return c.json({ user: userData });
  } catch (error) {
    console.log(`Error getting current user: ${error}`);
    return c.json({ error: 'Internal server error' }, 500);
  }
});

// Add vehicle (Driver only)
app.post('/make-server-c0c28b62/vehicles', async (c) => {
  try {
    const accessToken = c.req.header('Authorization')?.split(' ')[1];
    const { data: { user }, error: authError } = await supabase.auth.getUser(accessToken);
    
    if (!user || authError) {
      return c.json({ error: 'Unauthorized' }, 401);
    }

    const body = await c.req.json();
    const { model, vin, plate_number } = body;

    if (!model || !vin || !plate_number) {
      return c.json({ error: 'All fields are required' }, 400);
    }

    const vehicleId = crypto.randomUUID();
    const vehicle = {
      id: vehicleId,
      driver_id: user.id,
      model,
      vin,
      plate_number,
      created_at: new Date().toISOString()
    };

    await kv.set(`vehicle:${vehicleId}`, vehicle);
    await kv.set(`driver_vehicle:${user.id}`, vehicleId);

    return c.json({ success: true, vehicle });
  } catch (error) {
    console.log(`Error adding vehicle: ${error}`);
    return c.json({ error: 'Internal server error' }, 500);
  }
});

// Get driver's vehicles
app.get('/make-server-c0c28b62/vehicles', async (c) => {
  try {
    const accessToken = c.req.header('Authorization')?.split(' ')[1];
    const { data: { user }, error: authError } = await supabase.auth.getUser(accessToken);
    
    if (!user || authError) {
      return c.json({ error: 'Unauthorized' }, 401);
    }

    const vehicleId = await kv.get(`driver_vehicle:${user.id}`);
    if (!vehicleId) {
      return c.json({ vehicles: [] });
    }

    const vehicle = await kv.get(`vehicle:${vehicleId}`);
    return c.json({ vehicles: vehicle ? [vehicle] : [] });
  } catch (error) {
    console.log(`Error getting vehicles: ${error}`);
    return c.json({ error: 'Internal server error' }, 500);
  }
});

// Admin: Create staff account
app.post('/make-server-c0c28b62/admin/staff', async (c) => {
  try {
    const accessToken = c.req.header('Authorization')?.split(' ')[1];
    const { data: { user }, error: authError } = await supabase.auth.getUser(accessToken);
    
    if (!user || authError) {
      return c.json({ error: 'Unauthorized' }, 401);
    }

    const userData = await kv.get(`user:${user.id}`);
    if (userData.role !== 'admin') {
      return c.json({ error: 'Forbidden: Admin only' }, 403);
    }

    const body = await c.req.json();
    const { email, password, full_name, phone } = body;

    if (!email || !password || !full_name || !phone) {
      return c.json({ error: 'All fields are required' }, 400);
    }

    const { data, error } = await supabase.auth.admin.createUser({
      email,
      password,
      user_metadata: { 
        full_name,
        phone,
        role: 'staff'
      },
      email_confirm: true
    });

    if (error) {
      console.log(`Error creating staff: ${error.message}`);
      return c.json({ error: error.message }, 400);
    }

    await kv.set(`user:${data.user.id}`, {
      id: data.user.id,
      email,
      full_name,
      phone,
      role: 'staff',
      created_at: new Date().toISOString()
    });
    
    await kv.set(`user:${email}`, data.user.id);

    return c.json({ success: true, staff: {
      id: data.user.id,
      email,
      full_name,
      phone,
      role: 'staff'
    }});
  } catch (error) {
    console.log(`Error creating staff account: ${error}`);
    return c.json({ error: 'Internal server error' }, 500);
  }
});

// Get all users (Admin only)
app.get('/make-server-c0c28b62/admin/users', async (c) => {
  try {
    const accessToken = c.req.header('Authorization')?.split(' ')[1];
    const { data: { user }, error: authError } = await supabase.auth.getUser(accessToken);
    
    if (!user || authError) {
      return c.json({ error: 'Unauthorized' }, 401);
    }

    const userData = await kv.get(`user:${user.id}`);
    if (userData.role !== 'admin') {
      return c.json({ error: 'Forbidden: Admin only' }, 403);
    }

    const users = await kv.getByPrefix('user:');
    // Filter out email mapping keys (they contain @ symbol)
    const userList = users
      .filter((u: any) => u.value && typeof u.value === 'object' && u.value.id)
      .map((u: any) => u.value);

    return c.json({ users: userList });
  } catch (error) {
    console.log(`Error getting users: ${error}`);
    return c.json({ error: 'Internal server error' }, 500);
  }
});

// Create station (Admin only)
app.post('/make-server-c0c28b62/admin/stations', async (c) => {
  try {
    const accessToken = c.req.header('Authorization')?.split(' ')[1];
    const { data: { user }, error: authError } = await supabase.auth.getUser(accessToken);
    
    if (!user || authError) {
      return c.json({ error: 'Unauthorized' }, 401);
    }

    const userData = await kv.get(`user:${user.id}`);
    if (userData.role !== 'admin') {
      return c.json({ error: 'Forbidden: Admin only' }, 403);
    }

    const body = await c.req.json();
    const { name, address, latitude, longitude } = body;

    const stationId = crypto.randomUUID();
    const station = {
      id: stationId,
      name,
      address,
      latitude,
      longitude,
      status: 'active',
      created_at: new Date().toISOString()
    };

    await kv.set(`station:${stationId}`, station);

    return c.json({ success: true, station });
  } catch (error) {
    console.log(`Error creating station: ${error}`);
    return c.json({ error: 'Internal server error' }, 500);
  }
});

// Get all stations
app.get('/make-server-c0c28b62/stations', async (c) => {
  try {
    const stations = await kv.getByPrefix('station:');
    const stationList = stations.map((s: any) => s.value);
    return c.json({ stations: stationList });
  } catch (error) {
    console.log(`Error getting stations: ${error}`);
    return c.json({ error: 'Internal server error' }, 500);
  }
});

// Create battery (Admin/Staff)
app.post('/make-server-c0c28b62/batteries', async (c) => {
  try {
    const accessToken = c.req.header('Authorization')?.split(' ')[1];
    const { data: { user }, error: authError } = await supabase.auth.getUser(accessToken);
    
    if (!user || authError) {
      return c.json({ error: 'Unauthorized' }, 401);
    }

    const userData = await kv.get(`user:${user.id}`);
    if (userData.role !== 'admin' && userData.role !== 'staff') {
      return c.json({ error: 'Forbidden: Admin or Staff only' }, 403);
    }

    const body = await c.req.json();
    const { station_id, serial_number, capacity, status } = body;

    const batteryId = crypto.randomUUID();
    const battery = {
      id: batteryId,
      station_id,
      serial_number,
      capacity,
      status: status || 'available',
      charge_level: 100,
      created_at: new Date().toISOString()
    };

    await kv.set(`battery:${batteryId}`, battery);

    return c.json({ success: true, battery });
  } catch (error) {
    console.log(`Error creating battery: ${error}`);
    return c.json({ error: 'Internal server error' }, 500);
  }
});

// Get batteries by station
app.get('/make-server-c0c28b62/batteries/:stationId', async (c) => {
  try {
    const stationId = c.req.param('stationId');
    const batteries = await kv.getByPrefix('battery:');
    const stationBatteries = batteries
      .map((b: any) => b.value)
      .filter((b: any) => b.station_id === stationId);
    
    return c.json({ batteries: stationBatteries });
  } catch (error) {
    console.log(`Error getting batteries: ${error}`);
    return c.json({ error: 'Internal server error' }, 500);
  }
});

// Create swap transaction (Staff)
app.post('/make-server-c0c28b62/transactions', async (c) => {
  try {
    const accessToken = c.req.header('Authorization')?.split(' ')[1];
    const { data: { user }, error: authError } = await supabase.auth.getUser(accessToken);
    
    if (!user || authError) {
      return c.json({ error: 'Unauthorized' }, 401);
    }

    const userData = await kv.get(`user:${user.id}`);
    if (userData.role !== 'staff') {
      return c.json({ error: 'Forbidden: Staff only' }, 403);
    }

    const body = await c.req.json();
    const { driver_id, vehicle_id, old_battery_id, new_battery_id, station_id } = body;

    const transactionId = crypto.randomUUID();
    const transaction = {
      id: transactionId,
      driver_id,
      vehicle_id,
      staff_id: user.id,
      old_battery_id,
      new_battery_id,
      station_id,
      timestamp: new Date().toISOString(),
      status: 'completed'
    };

    await kv.set(`transaction:${transactionId}`, transaction);

    return c.json({ success: true, transaction });
  } catch (error) {
    console.log(`Error creating transaction: ${error}`);
    return c.json({ error: 'Internal server error' }, 500);
  }
});

// Get transactions (Driver sees their own, Staff/Admin see all)
app.get('/make-server-c0c28b62/transactions', async (c) => {
  try {
    const accessToken = c.req.header('Authorization')?.split(' ')[1];
    const { data: { user }, error: authError } = await supabase.auth.getUser(accessToken);
    
    if (!user || authError) {
      return c.json({ error: 'Unauthorized' }, 401);
    }

    const userData = await kv.get(`user:${user.id}`);
    const transactions = await kv.getByPrefix('transaction:');
    
    let transactionList = transactions.map((t: any) => t.value);
    
    if (userData.role === 'driver') {
      transactionList = transactionList.filter((t: any) => t.driver_id === user.id);
    }

    return c.json({ transactions: transactionList });
  } catch (error) {
    console.log(`Error getting transactions: ${error}`);
    return c.json({ error: 'Internal server error' }, 500);
  }
});

// Create support ticket
app.post('/make-server-c0c28b62/tickets', async (c) => {
  try {
    const accessToken = c.req.header('Authorization')?.split(' ')[1];
    const { data: { user }, error: authError } = await supabase.auth.getUser(accessToken);
    
    if (!user || authError) {
      return c.json({ error: 'Unauthorized' }, 401);
    }

    const body = await c.req.json();
    const { subject, description } = body;

    const ticketId = crypto.randomUUID();
    const ticket = {
      id: ticketId,
      user_id: user.id,
      subject,
      description,
      status: 'open',
      created_at: new Date().toISOString()
    };

    await kv.set(`ticket:${ticketId}`, ticket);

    return c.json({ success: true, ticket });
  } catch (error) {
    console.log(`Error creating ticket: ${error}`);
    return c.json({ error: 'Internal server error' }, 500);
  }
});

// Get tickets (Driver sees their own, Staff/Admin see all)
app.get('/make-server-c0c28b62/tickets', async (c) => {
  try {
    const accessToken = c.req.header('Authorization')?.split(' ')[1];
    const { data: { user }, error: authError } = await supabase.auth.getUser(accessToken);
    
    if (!user || authError) {
      return c.json({ error: 'Unauthorized' }, 401);
    }

    const userData = await kv.get(`user:${user.id}`);
    const tickets = await kv.getByPrefix('ticket:');
    
    let ticketList = tickets.map((t: any) => t.value);
    
    if (userData.role === 'driver') {
      ticketList = ticketList.filter((t: any) => t.user_id === user.id);
    }

    return c.json({ tickets: ticketList });
  } catch (error) {
    console.log(`Error getting tickets: ${error}`);
    return c.json({ error: 'Internal server error' }, 500);
  }
});

// Update ticket status (Staff/Admin)
app.patch('/make-server-c0c28b62/tickets/:id', async (c) => {
  try {
    const accessToken = c.req.header('Authorization')?.split(' ')[1];
    const { data: { user }, error: authError } = await supabase.auth.getUser(accessToken);
    
    if (!user || authError) {
      return c.json({ error: 'Unauthorized' }, 401);
    }

    const userData = await kv.get(`user:${user.id}`);
    if (userData.role !== 'admin' && userData.role !== 'staff') {
      return c.json({ error: 'Forbidden: Admin or Staff only' }, 403);
    }

    const ticketId = c.req.param('id');
    const body = await c.req.json();
    const { status } = body;

    const ticket = await kv.get(`ticket:${ticketId}`);
    if (!ticket) {
      return c.json({ error: 'Ticket not found' }, 404);
    }

    const updatedTicket = {
      ...ticket,
      status,
      updated_at: new Date().toISOString()
    };

    await kv.set(`ticket:${ticketId}`, updatedTicket);

    return c.json({ success: true, ticket: updatedTicket });
  } catch (error) {
    console.log(`Error updating ticket: ${error}`);
    return c.json({ error: 'Internal server error' }, 500);
  }
});

// Get available batteries at a station
app.get('/make-server-c0c28b62/stations/:stationId/available-batteries', async (c) => {
  try {
    const stationId = c.req.param('stationId');
    const batteries = await kv.getByPrefix('battery:');
    
    // Filter batteries that are at this station and available (SOC >= 80%)
    const availableBatteries = batteries
      .map((b: any) => b.value)
      .filter((b: any) => b.station_id === stationId && b.soc_percent >= 80 && b.state === 'available')
      .sort((a: any, b: any) => b.soc_percent - a.soc_percent); // Sort by highest charge first
    
    return c.json({ batteries: availableBatteries });
  } catch (error) {
    console.log(`Error getting available batteries: ${error}`);
    return c.json({ error: 'Internal server error' }, 500);
  }
});

// Get available time slots for reservation
app.get('/make-server-c0c28b62/stations/:stationId/timeslots', async (c) => {
  try {
    const stationId = c.req.param('stationId');
    const date = c.req.query('date') || new Date().toISOString().split('T')[0];
    
    // Generate time slots for the day (every 30 minutes from 6:00 to 22:00)
    const slots = [];
    const startHour = 6;
    const endHour = 22;
    
    for (let hour = startHour; hour < endHour; hour++) {
      for (let minute of [0, 30]) {
        const timeStr = `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;
        const datetime = new Date(`${date}T${timeStr}:00`);
        
        // Only show future slots
        if (datetime > new Date()) {
          // Check existing reservations for this slot
          const reservations = await kv.getByPrefix('reservation:');
          const slotReservations = reservations
            .map((r: any) => r.value)
            .filter((r: any) => {
              const reservedTime = new Date(r.reserved_start);
              return r.station_id === stationId && 
                     reservedTime.toISOString().substring(0, 16) === datetime.toISOString().substring(0, 16) &&
                     (r.status === 'pending' || r.status === 'confirmed');
            });
          
          // Assume each station can handle 5 concurrent swaps
          const available = 5 - slotReservations.length;
          
          slots.push({
            time: timeStr,
            datetime: datetime.toISOString(),
            available,
            total: 5
          });
        }
      }
    }
    
    return c.json({ slots });
  } catch (error) {
    console.log(`Error getting time slots: ${error}`);
    return c.json({ error: 'Internal server error' }, 500);
  }
});

// Create reservation (Driver)
app.post('/make-server-c0c28b62/reservations', async (c) => {
  try {
    const accessToken = c.req.header('Authorization')?.split(' ')[1];
    const { data: { user }, error: authError } = await supabase.auth.getUser(accessToken);
    
    if (!user || authError) {
      return c.json({ error: 'Unauthorized' }, 401);
    }

    const userData = await kv.get(`user:${user.id}`);
    
    const body = await c.req.json();
    const { vehicle_id, station_id, reserved_start } = body;

    if (!vehicle_id || !station_id || !reserved_start) {
      return c.json({ error: 'Missing required fields' }, 400);
    }

    // Verify vehicle belongs to user
    const vehicle = await kv.get(`vehicle:${vehicle_id}`);
    if (!vehicle || vehicle.driver_id !== user.id) {
      return c.json({ error: 'Vehicle not found or unauthorized' }, 404);
    }

    // Verify station exists
    const station = await kv.get(`station:${station_id}`);
    if (!station) {
      return c.json({ error: 'Station not found' }, 404);
    }

    // Create reservation
    const reservationId = crypto.randomUUID();
    const qrToken = crypto.randomUUID();
    const qrNonce = crypto.randomUUID();
    const reservedStartDate = new Date(reserved_start);
    const reservedEndDate = new Date(reservedStartDate.getTime() + 30 * 60 * 1000); // 30 minutes window
    
    const reservation = {
      id: reservationId,
      driver_id: user.id,
      vehicle_id,
      station_id,
      reserved_start: reservedStartDate.toISOString(),
      reserved_end: reservedEndDate.toISOString(),
      status: 'confirmed',
      qr_code: `EVS-${reservationId.substring(0, 8).toUpperCase()}`,
      created_at: new Date().toISOString(),
      updated_at: new Date().toISOString()
    };

    await kv.set(`reservation:${reservationId}`, reservation);

    // Get vehicle and station details for response
    return c.json({ 
      success: true, 
      reservation: {
        ...reservation,
        vehicle,
        station
      }
    });
  } catch (error) {
    console.log(`Error creating reservation: ${error}`);
    return c.json({ error: 'Internal server error' }, 500);
  }
});

// Get reservations for driver
app.get('/make-server-c0c28b62/reservations', async (c) => {
  try {
    const accessToken = c.req.header('Authorization')?.split(' ')[1];
    const { data: { user }, error: authError } = await supabase.auth.getUser(accessToken);
    
    if (!user || authError) {
      return c.json({ error: 'Unauthorized' }, 401);
    }

    const reservations = await kv.getByPrefix('reservation:');
    const userReservations = reservations
      .map((r: any) => r.value)
      .filter((r: any) => r.driver_id === user.id)
      .sort((a: any, b: any) => new Date(b.created_at).getTime() - new Date(a.created_at).getTime());
    
    return c.json({ reservations: userReservations });
  } catch (error) {
    console.log(`Error getting reservations: ${error}`);
    return c.json({ error: 'Internal server error' }, 500);
  }
});

// Cancel/Delete reservation
app.delete('/make-server-c0c28b62/reservations/:reservationId', async (c) => {
  try {
    const accessToken = c.req.header('Authorization')?.split(' ')[1];
    const { data: { user }, error: authError } = await supabase.auth.getUser(accessToken);
    
    if (!user || authError) {
      return c.json({ error: 'Unauthorized' }, 401);
    }

    const reservationId = c.req.param('reservationId');
    const reservation = await kv.get(`reservation:${reservationId}`);
    
    if (!reservation) {
      return c.json({ error: 'Reservation not found' }, 404);
    }

    // Check if user owns this reservation
    if (reservation.driver_id !== user.id) {
      return c.json({ error: 'Unauthorized - not your reservation' }, 403);
    }

    // Update reservation status to cancelled instead of deleting
    const updatedReservation = {
      ...reservation,
      status: 'cancelled',
      cancelled_at: new Date().toISOString(),
      updated_at: new Date().toISOString()
    };

    await kv.set(`reservation:${reservationId}`, updatedReservation);

    return c.json({ 
      success: true, 
      message: 'Reservation cancelled successfully',
      reservation: updatedReservation 
    });
  } catch (error) {
    console.log(`Error cancelling reservation: ${error}`);
    return c.json({ error: 'Internal server error' }, 500);
  }
});

// Get feedbacks for current user (Driver)
app.get('/make-server-c0c28b62/feedbacks', async (c) => {
  try {
    const accessToken = c.req.header('Authorization')?.split(' ')[1];
    const { data: { user }, error: authError } = await supabase.auth.getUser(accessToken);
    
    if (!user || authError) {
      return c.json({ error: 'Unauthorized' }, 401);
    }

    const feedbacks = await kv.getByPrefix('feedback:');
    const userFeedbacks = feedbacks
      .map((f: any) => f.value)
      .filter((f: any) => f.driver_id === user.id)
      .sort((a: any, b: any) => new Date(b.created_at).getTime() - new Date(a.created_at).getTime());
    
    return c.json({ feedbacks: userFeedbacks });
  } catch (error) {
    console.log(`Error getting feedbacks: ${error}`);
    return c.json({ error: 'Internal server error' }, 500);
  }
});

// Create feedback (Driver)
app.post('/make-server-c0c28b62/feedbacks', async (c) => {
  try {
    const accessToken = c.req.header('Authorization')?.split(' ')[1];
    const { data: { user }, error: authError } = await supabase.auth.getUser(accessToken);
    
    if (!user || authError) {
      return c.json({ error: 'Unauthorized' }, 401);
    }

    const body = await c.req.json();
    const { transaction_id, station_id, rating, comment } = body;

    if (!transaction_id || !station_id || !rating) {
      return c.json({ error: 'Missing required fields' }, 400);
    }

    if (rating < 1 || rating > 5) {
      return c.json({ error: 'Rating must be between 1 and 5' }, 400);
    }

    const feedbackId = crypto.randomUUID();
    const feedback = {
      id: feedbackId,
      driver_id: user.id,
      transaction_id,
      station_id,
      rating,
      comment: comment || '',
      created_at: new Date().toISOString()
    };

    await kv.set(`feedback:${feedbackId}`, feedback);

    return c.json({ success: true, feedback });
  } catch (error) {
    console.log(`Error creating feedback: ${error}`);
    return c.json({ error: 'Internal server error' }, 500);
  }
});

Deno.serve(app.fetch);
