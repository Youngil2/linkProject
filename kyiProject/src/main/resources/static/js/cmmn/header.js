function logoutAndRedirect() {
    fetch('/logout', {
      method: 'POST',
      headers: {
        'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
      }
    })
    .then(() => {
      location.href = '/login';
    })
    .catch(err => {
      console.error('로그아웃 실패:', err);
    });
  }