using Microsoft.AspNetCore.Mvc;
using WebApiObjetos.Domain;
using WebApiObjetos.Services.Interfaces;
using System;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;

namespace WebApiObjetos.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class UserController : Controller
    {
        private IUserService userService;

        public UserController(IUserService userService)
        {
            this.userService = userService;
        }

        [HttpPost]
        [Route("login"), AllowAnonymous]
        public async Task<IActionResult> Login(UserDTO user)
        {

            var obtainedUser = await userService.Login(user);

            if (obtainedUser == null)
                return Unauthorized();

            return Ok(obtainedUser);
        }


        [Route("SignIn")]
        [HttpPost]
        [ProducesResponseType(StatusCodes.Status201Created)]
        [AllowAnonymous]
        public async Task<IActionResult> SignIn([FromBody] UserDTO user)
        {
            if (!ModelState.IsValid)
                throw new InvalidOperationException("Invaid Model");

            var success = await userService.SignIn(user);
            if (!success)
                return Conflict("El usuario ya existe");

            return Created("El usuario ha sido registrado exitosamente", user);
        }
        
    }
}
