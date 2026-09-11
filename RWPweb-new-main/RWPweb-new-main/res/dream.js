        document.addEventListener('DOMContentLoaded', function() {
            const startupAnimation = document.getElementById('rwanim');

            setTimeout(function() {
                startupAnimation.classList.add('fade-out');

                setTimeout(function() {
                    startupAnimation.style.display = 'none';
                }, 500);
            }, 1200);

            document.querySelectorAll('.nav-button').forEach(button => {
                button.addEventListener('click', function() {
                    const url = this.getAttribute('data-url');
                    if (url) {
                        window.open(url, '_blank');
                    }
                    if (window.innerWidth <= 768) {
                        closeMenu();
                    }
                });
            });

            const body = document.querySelector('body');
            const colors = ['#ff6b6b', '#4ecdc4', '#ffe66d', '#ff8e8e', '#6aecd4'];

            for (let i = 0; i < 10; i++) {
                const pixel = document.createElement('div');
                pixel.classList.add('rwanimb');
                
                const size = Math.random() * 20 + 10;
                pixel.style.width = `${size}px`;
                pixel.style.height = `${size}px`;
                pixel.style.background = colors[Math.floor(Math.random() * colors.length)];
                
                const x = Math.random() * 90 + 5;
                const y = Math.random() * 90 + 5; 
                pixel.style.setProperty('--x', `${x}%`);
                pixel.style.setProperty('--y', `${y}%`);
                
                pixel.style.animationDelay = `${Math.random() * 5}s`;
                pixel.style.animationDuration = `${Math.random() * 5 + 5}s`;

                body.appendChild(pixel);
            }

            var OriginTitile = document.title,
            st;
            document.addEventListener("visibilitychange",
            function() {
                document.hidden ? (document.title = "(=^･ｪ･^=)", clearTimeout(st)) : (document.title = "(๑˃̵ᴗ˂̵)", st = setTimeout(function() {
                    document.title = OriginTitile
                }, 3000));
            });

            const mobilemenu = document.getElementById('mobilemenu');
            const navButtons = document.getElementById('navButtons');
            let isMenuOpen = false;
            
            function closeMenu() {
                if (window.innerWidth <= 768) {
                    navButtons.classList.remove('active');
                    mobilemenu.classList.remove('active');
                    isMenuOpen = false;
                    
                    setTimeout(() => {
                        if (!isMenuOpen && window.innerWidth <= 768) {
                            navButtons.style.display = 'none';
                        }
                    }, 300);
                }
            }
            
            function openMenu() {
                if (window.innerWidth <= 768) {
                    navButtons.style.display = 'flex';
                    navButtons.offsetHeight;
                    navButtons.classList.add('active');
                    mobilemenu.classList.add('active');
                    isMenuOpen = true;
                }
            }
            
            mobilemenu.addEventListener('click', function() {
                if (isMenuOpen) {
                    closeMenu();
                } else {
                    openMenu();
                }
            });

            document.addEventListener('click', function(event) {
                const isClickInsideMenu = navButtons.contains(event.target) || mobilemenu.contains(event.target);
                
                if (!isClickInsideMenu && isMenuOpen && window.innerWidth <= 768) {
                    closeMenu();
                }
            });
            
            window.addEventListener('resize', function() {
  
                if (window.innerWidth > 768) {
                    navButtons.style.display = 'flex';
                    navButtons.classList.remove('active');
                    mobilemenu.classList.remove('active');
                    isMenuOpen = false;
                } else {
                    if (!isMenuOpen) {
                        navButtons.style.display = 'none';
                    }
                }
            });
            

            function checkInitialState() {
                if (window.innerWidth > 768) {
                    navButtons.style.display = 'flex';
                } else {
                    navButtons.style.display = 'none';
                }
            }
            

            checkInitialState();
        });