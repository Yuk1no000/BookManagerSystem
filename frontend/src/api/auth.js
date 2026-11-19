import {ref} from 'vue';


export const currentUser = ref(null);

export async function loadCurrentUser() {
    const response = await fetch('/api/auth/me');
    if(response.status==401){
        currentUser.value = null;
        return
    }

    if(!response.ok){
        throw new Error('Failed to load current user Http: ' + response.status);
    }
    currentUser.value = await response.json();
}

export async function csrfHeaders(){
    const response = await fetch('/api/auth/csrf');
    if(!response.ok){
        throw new Error('Failed to load CSRF token Http: ' + response.status);
    }
    const {headerName,token} = await response.json();
    return {
        [headerName]: token
    }
}

export async function login(username, password) {
    const response = await fetch('/api/auth/login', {
        method:'POST',
        headers:await csrfHeaders(),
        body: new URLSearchParams({username, password})
    })

    if(!response.ok){
        throw new Error('Failed to login Http: ' + response.status);
    }
    if(response.status==401){
        currentUser.value = null;
        return false;
    }
    await loadCurrentUser()
}

export async function logout() {
    const response = await fetch('/api/auth/logout', {
        method:'POST',
        headers:await csrfHeaders()
    })

    if(!response.ok){
        throw new Error('Failed to logout Http: ' + response.status);
    }
    currentUser.value = null;
}

export function hasRouteAccess(route) {
    const user = currentUser.value
    const roles = route.meta.roles

    return Boolean(user && (!roles || roles.some(role => user.roles.includes(role))))
}