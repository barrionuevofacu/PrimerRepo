using Microsoft.AspNetCore.Http;
using Microsoft.IdentityModel.Tokens;
using System;
using System.Collections.Generic;
using System.IdentityModel.Tokens.Jwt;
using System.Linq;
using System.Security.Claims;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;
using WebApiObjetos.Domain;
using WebApiObjetos.Models.Entities;
using WebApiObjetos.Models.Repositories;
using WebApiObjetos.Models.Repositories.Interfaces;
using WebApiObjetos.Properties;
using WebApiObjetos.Services.Interfaces;



namespace WebApiObjetos.Services
{
    //representa las columnas de la tabla, tiene sus parametros
    public class UserService : IUserService
    {
        private IUserRepository userRepo;

        public UserService(IUserRepository userRepo)
        {
            this.userRepo = userRepo;
        }

        
        public async Task<bool> SignIn(UserDTO user)
        {
            var existingUser = await userRepo.FindBy(x => x.UserName == user.UserName);

            if (existingUser.Count != 0)
                return false;

            await userRepo.Add(user.ToEntity());
            return true;
        }


        public async Task<UserDTO> Login(UserDTO user)
        {

            var result = (await userRepo.FindBy(x => x.UserName == user.UserName && x.Password == user.Password)).First();

            if (result == null)
                return null;

            var token = GenerateToken(result);

            UserDTO userDto = new UserDTO()
            {
                UserName = user.UserName,
                Password = user.Password,
                Token = new JwtSecurityTokenHandler().WriteToken(token),
            };
        
            return userDto;
        }
        

        private JwtSecurityToken GenerateToken(User user)
        {
            try
            {
                var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(Resources.Encription_Key));
                var cred = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);
                // cuando la base tenga mas datos importantes tipo version, edad , etc, eso se mete en una claim para que no tenga que enviar esa info en cada request de los servicios
                var claim = (new[] {
                new Claim(JwtRegisteredClaimNames.Sub, user.UserName),
                new Claim(JwtRegisteredClaimNames.Iat,DateTime.UtcNow.ToString()),
                new Claim(JwtRegisteredClaimNames.Jti, Guid.NewGuid().ToString()),
                new Claim(ClaimTypes.Role,"admin"),
                new Claim("UserId", user.Id.ToString())
            });

                return new JwtSecurityToken(
                            issuer: Resources.Issuer,
                            audience: Resources.Audience,
                            claims: claim,
                            expires: DateTime.UtcNow.AddHours(Int32.Parse(Resources.Token_Duration)),//está puesto una hora para el token, se cambia en la tabla resources
                            notBefore: DateTime.UtcNow, // a partir de cuando se puede usar el token
                            signingCredentials: cred
                                            );
            }
            catch (Exception e)
            {
                throw e;
            }
        }



        //este método se usa para extraer las claims del token vencido. // no se usa pero puede resultar util para despues
        private ClaimsPrincipal GetPrincipalFromExpiredToken(string token)
        {
            var tokenValidationParameters = new TokenValidationParameters
            {
                ValidateAudience = false,
                ValidateIssuer = false,
                ValidateIssuerSigningKey = true,
                IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(Resources.Encription_Key)),
                ValidateLifetime = false //le digo que no verifique la fecha.
            };

            var tokenHandler = new JwtSecurityTokenHandler();
            SecurityToken securityToken;

            var principal = tokenHandler.ValidateToken(token, tokenValidationParameters, out securityToken);
            var jwtSecurityToken = securityToken as JwtSecurityToken;
            if (jwtSecurityToken == null || !jwtSecurityToken.Header.Alg.Equals(SecurityAlgorithms.HmacSha256, StringComparison.InvariantCultureIgnoreCase))
                throw new SecurityTokenException("Invalid token");

            return principal;
        }


    }
}
