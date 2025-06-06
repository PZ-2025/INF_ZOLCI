export const useValidation = () => {
  
  // Walidacja użytkownika
  const validateUser = (userData, isUpdate = false) => {
    const errors = [];
    
    // Username: 3-50 znaków
    if (!userData.username || userData.username.trim().length < 3) {
      errors.push('Nazwa użytkownika musi mieć co najmniej 3 znaki');
    }
    if (userData.username && userData.username.length > 50) {
      errors.push('Nazwa użytkownika nie może przekraczać 50 znaków');
    }
    
    // Hasło: 6+ znaków (tylko przy tworzeniu nowego lub gdy podano)
    if (!isUpdate && (!userData.password || userData.password.length < 6)) {
      errors.push('Hasło musi mieć co najmniej 6 znaków');
    }
    if (isUpdate && userData.password && userData.password.length < 6) {
      errors.push('Hasło musi mieć co najmniej 6 znaków');
    }
    
    // Imię: 1-50 znaków
    if (!userData.firstName || userData.firstName.trim().length < 1) {
      errors.push('Imię jest wymagane');
    }
    if (userData.firstName && userData.firstName.length > 50) {
      errors.push('Imię nie może przekraczać 50 znaków');
    }
    
    // Nazwisko: 1-50 znaków
    if (!userData.lastName || userData.lastName.trim().length < 1) {
      errors.push('Nazwisko jest wymagane');
    }
    if (userData.lastName && userData.lastName.length > 50) {
      errors.push('Nazwisko nie może przekraczać 50 znaków');
    }
    
    // Email: format + max 100
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!userData.email || !emailRegex.test(userData.email)) {
      errors.push('Nieprawidłowy format adresu email');
    }
    if (userData.email && userData.email.length > 100) {
      errors.push('Email nie może przekraczać 100 znaków');
    }
    
    // Telefon: opcjonalny, max 15 znaków, podstawowy format
    if (userData.phone && userData.phone.length > 15) {
      errors.push('Numer telefonu nie może przekraczać 15 znaków');
    }
    if (userData.phone && userData.phone.trim() && !/^[\d\s\-\+\(\)]+$/.test(userData.phone)) {
      errors.push('Nieprawidłowy format numeru telefonu (tylko cyfry, spacje, +, -, (, ))');
    }
    
    return errors;
  };

  // Walidacja zadania
  const validateTask = (taskData) => {
    const errors = [];
    
    // Tytuł: 1-100 znaków
    if (!taskData.title || taskData.title.trim().length < 1) {
      errors.push('Tytuł zadania jest wymagany');
    }
    if (taskData.title && taskData.title.length > 100) {
      errors.push('Tytuł zadania nie może przekraczać 100 znaków');
    }
    
    // Wymagane pola
    if (!taskData.teamId) {
      errors.push('Zespół jest wymagany');
    }
    if (!taskData.priorityId) {
      errors.push('Priorytet jest wymagany');
    }
    if (!taskData.statusId) {
      errors.push('Status jest wymagany');
    }
    
    // Walidacja dat
    if (taskData.startDate && taskData.deadline) {
      const startDate = new Date(taskData.startDate);
      const deadline = new Date(taskData.deadline);
      
      if (deadline < startDate) {
        errors.push('Deadline nie może być wcześniejszy niż data rozpoczęcia');
      }
    }
    
    if (taskData.completedDate && taskData.startDate) {
      const completedDate = new Date(taskData.completedDate);
      const startDate = new Date(taskData.startDate);
      
      if (completedDate < startDate) {
        errors.push('Data zakończenia nie może być wcześniejsza niż data rozpoczęcia');
      }
    }
    
    return errors;
  };

  // Walidacja zespołu
  const validateTeam = (teamData) => {
    const errors = [];
    
    // Nazwa: 2-100 znaków
    if (!teamData.name || teamData.name.trim().length < 2) {
      errors.push('Nazwa zespołu musi mieć co najmniej 2 znaki');
    }
    if (teamData.name && teamData.name.length > 100) {
      errors.push('Nazwa zespołu nie może przekraczać 100 znaków');
    }
    
    // Manager wymagany
    if (!teamData.managerId) {
      errors.push('Manager zespołu jest wymagany');
    }
    
    return errors;
  };

  // Walidacja hasła (dla potwierdzenia)
  const validatePasswordMatch = (password, confirmPassword) => {
    if (password !== confirmPassword) {
      return 'Hasła nie są identyczne';
    }
    return null;
  };

  return {
    validateUser,
    validateTask,
    validateTeam,
    validatePasswordMatch
  };
};