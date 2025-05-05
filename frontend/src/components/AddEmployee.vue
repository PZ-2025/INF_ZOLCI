<template>
    <div class="min-h-screen flex items-center justify-center bg-background text-text px-4">
      <form @submit.prevent="addEmployee" class="bg-surface p-6 rounded-lg shadow-md border border-gray-200 space-y-4 w-full">

        <h1 class="text-2xl font-bold text-primary mb-4">Dodaj Pracownika</h1>
        
        <div class="flex flex-col">
          <label for="first_name" class="block text-lg font-medium mb-2">Imię</label>
          <input
            v-model="user.first_name"
            id="first_name"
            type="text"
            required
            class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>
  
        <div class="flex flex-col">
          <label for="last_name" class="block text-lg font-medium mb-2">Nazwisko</label>
          <input
            v-model="user.last_name"
            id="last_name"
            type="text"
            required
            class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>
  
        <div class="flex flex-col">
          <label for="email" class="block text-lg font-medium mb-2">Email</label>
          <input
            v-model="user.email"
            id="email"
            type="email"
            required
            class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>
  
        <div class="flex flex-col">
          <label for="username" class="block text-lg font-medium mb-2">Nazwa użytkownika</label>
          <input
            v-model="user.username"
            id="username"
            type="text"
            required
            class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>
  
        <div class="flex flex-col">
          <label for="password" class="block text-lg font-medium mb-2">Hasło</label>
          <input
            v-model="user.password"
            id="password"
            type="password"
            required
            class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>
  
        <div class="flex flex-col">
          <label for="role" class="block text-lg font-medium mb-2">Rola</label>
          <select
            v-model="user.role"
            id="role"
            required
            class="p-2 border border-gray-300 rounded-md bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option value="pracownik">Pracownik</option>
            <option value="kierownik">Kierownik</option>
          </select>
        </div>
  
        <button type="submit" class="bg-primary hover:bg-secondary text-white px-6 py-2 rounded-md transition w-full">
        Dodaj
      </button>
    </form>
  </div>
</template>

<script>
export default {
  data() {
    return {
      user: {
        first_name: '',
        last_name: '',
        email: '',
        username: '',
        password: '',
        role: 'pracownik'
      }
    };
  },
  methods: {
    addEmployee() {
      console.log("Dodano użytkownika:", this.user);
      // spot for api call to add employee
    }
  }
};
</script>
  