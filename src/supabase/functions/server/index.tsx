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

// Initialize admin account on first run
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

// Initialize admin on startup
initializeAdmin();

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
    const { model, vin, license_plate } = body;

    if (!model || !vin || !license_plate) {
      return c.json({ error: 'All fields are required' }, 400);
    }

    const vehicleId = crypto.randomUUID();
    const vehicle = {
      id: vehicleId,
      driver_id: user.id,
      model,
      vin,
      license_plate,
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

Deno.serve(app.fetch);
